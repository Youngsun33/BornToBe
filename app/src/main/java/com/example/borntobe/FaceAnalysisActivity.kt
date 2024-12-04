package com.example.borntobe


import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.borntobe.databinding.ActivityFaceAnalysisBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.system.exitProcess

class FaceAnalysisActivity : AppCompatActivity() {
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100 // 카메라 권한 요청 코드
    }

    private lateinit var binding: ActivityFaceAnalysisBinding
    private lateinit var ivImage: ImageView   // 이미지를 표시할 뷰
    private lateinit var buttonLoadImage: ImageButton  // 이미지를 불러올 버튼
    private lateinit var btnCamera: ImageButton
    private lateinit var btnAnalysis: ImageButton
    private lateinit var btnResult: Button
    private lateinit var dialog: Dialog
    private lateinit var image: Bitmap
    private lateinit var processedBitmap: Bitmap
    private lateinit var tflite: Interpreter
    private lateinit var db: FirebaseFirestore
    private var isResultScreen = false // 결과 화면 여부를 나타내는 플래그
    private val utils: Utils = Utils(this)

    // onBackPressedCallback : 뒤로 가기 동작을 정의하는 callback 메소드
    private var backKeyPressedTime = 0L
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                    if (isResultScreen) {
                        // 결과 화면에서 뒤로 가기 -> 초기 레이아웃 복원
                        val intent = Intent(this@FaceAnalysisActivity, FaceAnalysisActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        isResultScreen = false // 상태 초기화
                    } else {
                        backKeyPressedTime = System.currentTimeMillis()
                        utils.showToast("뒤로가기를 한번 더 누르면 종료됩니다.")

                        // 이전 화면 새로 갱신을 위해 Intent 전달
                        val intent = Intent(this@FaceAnalysisActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // 이전 화면을 새로 시작
                        startActivity(intent)
                        finish()
                    }
                } else {
                    exitProgram()
                }
            }
        }

    // isPhotoPickerAvailable : 사진 선택 도구가 사용 가능한 기기 인지 확인
    private val isPhotoPickerAvailable =
        ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)

    // pickMedia : launch() 메소드로 PhotoPicker를 열었다가 사용자가 선택한 사진을 가공해야 하므로 registerForActivityResult() 메소드를 활용
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                ivImage.scaleType = ImageView.ScaleType.CENTER_CROP
                ivImage.setImageURI(uri)
                utils.showToast("이미지가 업로드 되었습니다.")
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                image = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                utils.showToast("이미지를 선택하세요.")
            }
        }

    // 카메라 설정
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                ivImage.scaleType = ImageView.ScaleType.CENTER_CROP
                ivImage.setImageBitmap(bitmap)
                image = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                utils.showToast("사진이 성공적으로 업로드되었습니다.")
            } else {
                utils.showToast("사진 촬영을 취소했습니다.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 콜백 등록
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Firestore 데이터베이스 초기화
        db = Firebase.firestore

        val dataStore = DataStoreModule(this)
        // 현재 사용자 정보 가져오기
        var id = "NULL"
        var name = "NULL"
        var pw = "NULL"
        lifecycleScope.launch {
            dataStore.userNameFlow.collect { userName ->
                name = userName
                Log.i("A_Face", "User Name: $name")
            }
        }
        lifecycleScope.launch {
            dataStore.userIDFlow.collect { userID ->
                id = userID
                Log.i("A_Face", "User ID: $id")
            }
        }
        lifecycleScope.launch {
            dataStore.userPWFlow.collect { userPW ->
                pw = userPW
                Log.i("A_Face", "User PW: $pw")
            }
        }

        // 다이얼로그 설정
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_caution)
        dialog.findViewById<TextView>(R.id.dialog_caution_tvInfo01)
            .setText(R.string.dialog_caution_faceInfo01)
        dialog.findViewById<TextView>(R.id.dialog_caution_tvInfo02)
            .setText(R.string.dialog_caution_faceInfo02)
        dialog.findViewById<TextView>(R.id.dialog_caution_tvInfo03)
            .setText(R.string.dialog_caution_faceInfo03)
        dialog.findViewById<TextView>(R.id.dialog_caution_tvInfo04)
            .setText(R.string.dialog_caution_faceInfo04)
        dialog.findViewById<ImageView>(R.id.dialog_caution_ivIC)
            .setImageResource(R.drawable.ic_caution_face)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCanceledOnTouchOutside(true)
        showDialog()

        // 변수 연결
        ivImage = binding.activityFaceAnalysisIvImage
        buttonLoadImage = binding.activityFaceAnalysisBtnImageUpload
        tflite = Interpreter(loadModelFile())

        // 이미지 업로드 버튼
        buttonLoadImage.setOnClickListener {
            if (isPhotoPickerAvailable) {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                utils.showToast("갤러리 기능을 지원하지 않는 기기입니다.")
            }
        }

        btnCamera = binding.activityFaceAnalysisBtnCamera
        // 카메라로 사진 촬영
        btnCamera.setOnClickListener {
            checkCameraPermission()
        }

        btnAnalysis = binding.activityFaceAnalysisBtnAnalysis
        // 분석 버튼 클릭 리스너 추가
        btnAnalysis.setOnClickListener {
            // 인터넷 연결 확인
            if (!utils.isNetworkAvailable()) {
                utils.showToast("인터넷에 연결되어 있지 않습니다. \n연결 후 다시 시도해주세요.")
                return@setOnClickListener
            }

            var faceShape = "NULL"

            // 현재 사용자가 DB에 존재하는지 검사
            db.collection("users")
                .whereEqualTo("id", id)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // 문서가 존재하면 업데이트
                        val document = documents.documents[0] // 첫 번째 문서 가져오기
                        val documentRef = document.reference

                        // 이미지 크롭 및 리사이즈
                        preprocessImageForModel(image) { processedBitmap ->
                            // 얼굴 분석 실행
                            faceShape = analyzeFaceShapeWithModel(processedBitmap, tflite)
                        }

                        // DB에 사용자 체형 결과 저장
                        documentRef.update("face_shape", faceShape)
                            .addOnSuccessListener {
                                // 결과 화면으로 전환
                                btnResult = binding.activityFaceAnalysisBtnResult
                                btnResult.visibility = View.VISIBLE
                                btnResult.setOnClickListener {
                                    intent = Intent(this, ResultActivity::class.java)
                                    intent.putExtra("Activity", "Face")
                                    showFaceResultLayout(faceShape, name)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("FaceAnalysis", "Error adding document", e)
                                utils.showToast("결과를 저장할 수 없습니다.")
                            }
                    } else {
                        // 문서가 존재하지 않는 경우
                        utils.showToast("사용자 정보가 데이터베이스에 존재하지 않습니다.")
                    }
                }
                .addOnFailureListener {
                    utils.showToast("데이터베이스 조회 중 문제가 발생했습니다.")
                }
        }
    }

    // 얼굴형 결과 레이아웃 설정
    private fun showFaceResultLayout(faceShape: String, name: String) {
        isResultScreen = true // 결과 화면으로 변경
        when (faceShape) {
            "Oval" -> {
                setContentView(R.layout.activity_result_oval)
                findViewById<TextView>(R.id.oval_tvUserName)?.text = name
            }

            "Round" -> {
                setContentView(R.layout.activity_result_round)
                findViewById<TextView>(R.id.round_tvUserName)?.text = name
            }

            "Square" -> {
                setContentView(R.layout.activity_result_square)
                findViewById<TextView>(R.id.square_tvUserName)?.text = name
            }

            "Oblong" -> {
                setContentView(R.layout.activity_result_long)
                findViewById<TextView>(R.id.long_tvUserName)?.text = name
            }

            else -> utils.showToast("잘못된 얼굴형 결과입니다.")
        }
    }

    private fun preprocessImageForModel(
        bitmap: Bitmap,
        targetWidth: Int = 224,
        targetHeight: Int = 224,
        callback: (Bitmap) -> Unit
    ) {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .build()

        val faceDetector = FaceDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)

        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                val processedBitmap = if (faces.isNotEmpty()) {
                    val boundingBox = faces[0].boundingBox
                    val extendedBoundingBox = expandBoundingBox(
                        boundingBox, bitmap.width, bitmap.height, extraHeight = 10
                    )
                    cropAndResize(bitmap, extendedBoundingBox, targetWidth, targetHeight)
                } else {
                    cropAndResizeDefault(bitmap, targetWidth, targetHeight)
                }
                callback(processedBitmap)
            }
            .addOnFailureListener {
                val fallbackBitmap = cropAndResizeDefault(bitmap, targetWidth, targetHeight)
                callback(fallbackBitmap)
            }
    }

    // 바운딩 박스 확장 함수
    private fun expandBoundingBox(
        boundingBox: Rect,
        imageWidth: Int,
        imageHeight: Int,
        extraHeight: Int
    ): Rect {
        val x1 = boundingBox.left
        val y1 = boundingBox.top
        val x2 = boundingBox.right
        val y2 = boundingBox.bottom

        val newY1 = (y1 - extraHeight).coerceAtLeast(0)
        val newY2 = (y2 + extraHeight).coerceAtMost(imageHeight)
        val newHeight = newY2 - newY1

        val extraWidth = (newHeight - boundingBox.width()) / 2
        val newX1 = (x1 - extraWidth).coerceAtLeast(0)
        val newX2 = (x2 + extraWidth).coerceAtMost(imageWidth)

        return Rect(newX1, newY1, newX2, newY2)
    }

    // 크롭 및 리사이즈 함수
    private fun cropAndResize(
        bitmap: Bitmap,
        boundingBox: Rect,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap {
        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            boundingBox.left,
            boundingBox.top,
            boundingBox.width(),
            boundingBox.height()
        )
        return Bitmap.createScaledBitmap(croppedBitmap, targetWidth, targetHeight, true)
    }

    // 얼굴 감지 실패 시 기본 크롭 및 리사이즈
    private fun cropAndResizeDefault(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val inputAspectRatio = width.toFloat() / height.toFloat()
        val targetAspectRatio = targetWidth.toFloat() / targetHeight.toFloat()

        return if (inputAspectRatio > targetAspectRatio) {
            // 가로가 더 긴 경우
            val scaledWidth = (inputAspectRatio * targetHeight).toInt()
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, targetHeight, true)
            Bitmap.createBitmap(
                resizedBitmap,
                (scaledWidth - targetWidth) / 2,
                0,
                targetWidth,
                targetHeight
            )
        } else if (inputAspectRatio < targetAspectRatio) {
            // 세로가 더 긴 경우
            val scaledHeight = (targetWidth / inputAspectRatio).toInt()
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, scaledHeight, true)
            Bitmap.createBitmap(
                resizedBitmap,
                0,
                (scaledHeight - targetHeight) / 2,
                targetWidth,
                targetHeight
            )
        } else {
            // 비율이 동일한 경우
            Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        }
    }

    private fun loadModelFile(): ByteBuffer {
        val assetManager = assets
        val fileDescriptor = assetManager.openFd("face_analysis_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun preprocessImage(bitmap: Bitmap, inputSize: Int): ByteBuffer {
        val byteBuffer =
            ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3) // 4바이트 (Float) x 이미지 크기 x RGB 채널
        byteBuffer.order(ByteOrder.nativeOrder())

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)

        val intValues = IntArray(inputSize * inputSize)
        scaledBitmap.getPixels(
            intValues,
            0,
            scaledBitmap.width,
            0,
            0,
            scaledBitmap.width,
            scaledBitmap.height
        )

        for (pixel in intValues) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
    }

    // 얼굴형을 분석하는 메소드
    private fun analyzeFaceShapeWithModel(bitmap: Bitmap, tflite: Interpreter): String {
        // 1. 이미지 전처리
        val inputSize = 224 // 모델 입력 크기
        val inputBuffer = preprocessImage(bitmap, inputSize)

        // 2. 모델 출력 정의 (예: 얼굴형 분류라면 클래스 개수만큼 배열 생성)
        val output = Array(1) { FloatArray(4) }

        // 3. 추론 수행
        tflite.run(inputBuffer, output)

        // 4. 결과 해석
        val probabilities = output[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        val faceShape = when (maxIndex) {
            0 -> "Oblong"
            1 -> "Oval"
            2 -> "Round"
            3 -> "Square"
            else -> "Unknown"
        }

        // 5. 사용자에게 결과 표시
        showToast("분석 완료! 결과를 확인해보세요.")
        return faceShape
    }

    // 카메라 권한 확인 및 요청
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            openCamera()
        }
    }

    // 카메라 실행
    private fun openCamera() {
        takePicture.launch(null)
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openCamera()
            } else {
                utils.showToast("카메라 권한이 필요합니다.")
            }
        }
    }

    // 다이얼로그 보여주기
    private fun showDialog() {
        dialog.show()
        dialog.findViewById<Button>(R.id.dialog_caution_btnConfirm).setOnClickListener {
            dialog.dismiss()
        }
    }

    // 토스트 메시지를 띄우는 함수
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // 앱 종료
    private fun exitProgram() {
        moveTaskToBack(true)
        finishAndRemoveTask()
        exitProcess(0)
    }
}
