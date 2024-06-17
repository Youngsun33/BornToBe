package com.example.borntobe

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException

class FaceAnalysisActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView   // 이미지를 표시할 뷰
    private lateinit var buttonLoadImage: Button  // 이미지를 불러올 버튼
    private lateinit var pickMedia: ActivityResultLauncher<String>  // 이미지 선택을 위한 런처
    private var interpreter: Interpreter? = null  // TensorFlow Lite 인터프리터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_analysis)

        imageView = findViewById(R.id.imageView)
        buttonLoadImage = findViewById(R.id.buttonLoadImage)

        pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageView.setImageURI(uri)
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    classifyImage(bitmap)
                } catch (e: IOException) {
                    Toast.makeText(this, "이미지 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        buttonLoadImage.setOnClickListener {
            pickMedia.launch("image/*")
        }

        // TensorFlow Lite 모델 로드
        try {
            val model = FileUtil.loadMappedFile(this, "model_unquant.tflite")
            interpreter = Interpreter(model)
            Log.d("FaceAnalysisActivity", "모델 로드 성공")
        } catch (e: IOException) {
            Log.e("FaceAnalysisActivity", "모델 로드 실패: ${e.message}", e)
            Toast.makeText(this, "모델 로드 실패: ${e.message}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("FaceAnalysisActivity", "모델 초기화 중 오류 발생: ${e.message}", e)
            Toast.makeText(this, "모델 초기화 중 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 이미지를 분류하는 메소드
    private fun classifyImage(bitmap: Bitmap) {
        // 이미지 전처리
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        val intValues = IntArray(224 * 224)
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
        val floatValues = FloatArray(224 * 224 * 3)

        for (i in intValues.indices) {
            val value = intValues[i]
            floatValues[i * 3] = (value shr 16 and 0xFF) / 255.0f
            floatValues[i * 3 + 1] = (value shr 8 and 0xFF) / 255.0f
            floatValues[i * 3 + 2] = (value and 0xFF) / 255.0f
        }

        inputBuffer.loadArray(floatValues)

        // 예측 수행
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 5), DataType.FLOAT32)
        interpreter?.run(inputBuffer.buffer, outputBuffer.buffer.rewind())

        val output = outputBuffer.floatArray
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1

        // 예측 결과에 따라 얼굴형 표시
        val faceShape = when (maxIndex) {
            0 -> "하트형 얼굴"
            1 -> "길쭉한형 얼굴"
            2 -> "계란형 얼굴"
            3 -> "둥근형 얼굴"
            4 -> "네모형 얼굴"
            else -> "알 수 없는 얼굴형"
        }
        showToast(faceShape)
    }

    // 토스트 메시지를 띄우는 함수
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
