package com.example.borntobe


import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceContour
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException
import kotlin.math.pow

class FaceAnalysisActivity : AppCompatActivity() {

    private lateinit var detector: FaceDetector  // ML Kit 얼굴 인식 객체
    private lateinit var imageView: ImageView   // 이미지를 표시할 뷰
    private lateinit var buttonLoadImage: Button  // 이미지를 불러올 버튼
    private lateinit var pickMedia: ActivityResultLauncher<String>  // 이미지 선택을 위한 런처

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_face_analysis)

        imageView = findViewById(R.id.imageView)
        buttonLoadImage = findViewById(R.id.buttonLoadImage)

        // 얼굴 인식 옵션 설정
        val faceDetectorOptions = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)  // 정확도를 우선하는 모드
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)  // 모든 랜드마크 검출 활성화
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)  // 표정 등 분류 정보 활성화
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)  // 얼굴 윤곽 검출 활성화
            .build()

        detector = FaceDetection.getClient(faceDetectorOptions)

        pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageView.setImageURI(uri)
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    detectFaces(bitmap)
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
    }

    // 이미지에서 얼굴을 검출하고 결과를 처리
    fun detectFaces(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        detector.process(image)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    analyzeFaceShape(face)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "얼굴 인식 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 얼굴형을 분석하는 메소드
    private fun analyzeFaceShape(face: Face) {
        val contours = face.getContour(FaceContour.FACE)?.points ?: return
        // 얼굴의 주요 차원 측정
        val width = distance(contours[0], contours[8])
        val height = distance(contours[24], contours[8])
        val foreheadWidth = distance(contours[10], contours[19])
        val jawWidth = distance(contours[0], contours[17])

        // 기준에 따라 얼굴형 분류: 너비, 높이, 이마 너비, 턱 너비를 이용
        when {
            height > width && (foreheadWidth > jawWidth) -> showToast("하트형 얼굴")  // 더 높고, 이마가 턱보다 넓음
            height > width && (foreheadWidth < jawWidth) -> showToast("계란형 얼굴")  // 더 높고, 턱이 이마보다 넓음
            width > height -> showToast("둥근형 얼굴")  // 더 넓음
            foreheadWidth < width / 3 -> showToast("마름모형 얼굴")  // 이마가 전체 너비의 1/3보다 작음
            height / width > 1.5 -> showToast("땅콩형 얼굴")  // 높이가 너비보다 1.5배 더 큼
            else -> showToast("육각형 얼굴")  // 기타 모든 조건을 만족하지 않는 경우
        }
    }

    // 두 점 사이의 거리를 계산하는 함수
    private fun distance(p1: PointF, p2: PointF): Float {
        return Math.sqrt(((p2.x - p1.x).toDouble().pow(2.0) + (p2.y - p1.y).toDouble().pow(2.0)))
            .toFloat()
    }

    // 토스트 메시지를 띄우는 함수
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
