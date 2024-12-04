package com.example.borntobe

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.borntobe.databinding.ActivityHandAnalysisBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.system.exitProcess

class HandAnalysisActivity : AppCompatActivity() {
    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100 // 카메라 권한 요청 코드
    }

    // HandLandMark Detection 관련 변수
    private var results: HandLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()
    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    // ViewBinding 사용
    private lateinit var binding: ActivityHandAnalysisBinding

    // 위젯 변수
    private lateinit var ivImage: ImageView
    private lateinit var btnImageUpload: ImageButton
    private lateinit var btnCamera: ImageButton
    private lateinit var btnAnalysis: ImageButton
    private lateinit var btnResult: Button
    private lateinit var image: Bitmap
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var dialog: Dialog
    private val utils: Utils = Utils(this)
    private var isResultScreen = false // 결과 화면 여부를 나타내는 플래그
    private lateinit var db: FirebaseFirestore

    // onBackPressedCallback : 뒤로 가기 동작을 정의하는 callback 메소드
    private var backKeyPressedTime = 0L
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                    if (isResultScreen) {
                        // 결과 화면에서 뒤로 가기 -> 초기 레이아웃 복원
                        val intent = Intent(this@HandAnalysisActivity, HandAnalysisActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        isResultScreen = false // 상태 초기화
                    } else {
                        backKeyPressedTime = System.currentTimeMillis()
                        utils.showToast("뒤로가기를 한번 더 누르면 종료됩니다.")

                        // 이전 화면 새로 갱신을 위해 Intent 전달
                        val intent = Intent(this@HandAnalysisActivity, MainActivity::class.java)
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
        enableEdgeToEdge()
        binding = ActivityHandAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 콜백 등록
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // 시스템 바 영역 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                Log.i("A_Hand", "User Name: $name")
            }
        }
        lifecycleScope.launch {
            dataStore.userIDFlow.collect { userID ->
                id = userID
                Log.i("A_Hand", "User ID: $id")
            }
        }
        lifecycleScope.launch {
            dataStore.userPWFlow.collect { userPW ->
                pw = userPW
                Log.i("A_Hand", "User PW: $pw")
            }
        }

        // 다이얼로그 설정
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_caution)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCanceledOnTouchOutside(true)
        showDialog()

        // UI 요소 초기화
        ivImage = binding.activityHandAnalysisIvImage
        btnImageUpload = binding.activityHandAnalysisBtnImageUpload
        btnCamera = binding.activityHandAnalysisBtnCamera
        btnAnalysis = binding.activityHandAnalysisBtnAnalysis

        // 갤러리에서 이미지 업로드
        btnImageUpload.setOnClickListener {
            if (isPhotoPickerAvailable) {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else {
                utils.showToast("갤러리 기능을 지원하지 않는 기기입니다.")
            }
        }

        // 카메라로 사진 촬영
        btnCamera.setOnClickListener {
            checkCameraPermission()
        }

        // 손 분석을 위한 HandLandMarkerHelper 객체 생성
        handLandmarkerHelper = HandLandmarkerHelper(context = this)
        // 분석 버튼
        btnAnalysis.setOnClickListener {
            // 인터넷 연결 확인
            if (!utils.isNetworkAvailable()) {
                utils.showToast("인터넷에 연결되어 있지 않습니다. \n연결 후 다시 시도해주세요.")
                return@setOnClickListener
            }

            // 규격 이미지에서 LandMark 추출하여 결과 반환
            // 1. asset 폴더에서 규격 이미지 읽어오기
            val assetManager = resources.assets
            val inputStream = assetManager.open("hand_analysis_standard.jpg")
            val handAnalysisStandardBitmap = BitmapFactory.decodeStream(inputStream)
            val resizedStandardBitmap = Bitmap.createScaledBitmap(
                handAnalysisStandardBitmap,
                image.width,
                image.height,
                true
            )
            // 2. 규격 이미지 손 분석 수행
            val handStandardResult = handLandmarkerHelper.detectImage(resizedStandardBitmap)

            // 손 분석 결과로 추출된 LandMark 반환
            val resizedImg =
                Bitmap.createScaledBitmap(image, ivImage.width, ivImage.height, true)
            val handLandMarkerResult = handLandmarkerHelper.detectImage(resizedImg)
            // 결과값이 null이 아니면 landmark 그려서 보여줌
            if (handLandMarkerResult != null) {
                val canvas = Canvas(resizedImg)
                initPaints()
                setResults(handLandMarkerResult, resizedImg.height, resizedImg.width)
                draw(canvas)
                ivImage.scaleType = ImageView.ScaleType.FIT_XY
                ivImage.setImageBitmap(resizedImg)

                val handAnalysisHelper =
                    HandAnalysisHelper(handLandMarkerResult, handStandardResult!!)
                handAnalysisHelper.classifier()
                val bodyShape = handAnalysisHelper.analysisBodyShape()
                utils.showToast("분석 완료! 결과를 확인해보세요.")

                // 현재 사용자가 DB에 존재하는지 검사
                db.collection("users")
                    .whereEqualTo("id", id)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            // 문서가 존재하면 업데이트
                            val document = documents.documents[0] // 첫 번째 문서 가져오기
                            val documentRef = document.reference

                            // DB에 사용자 체형 결과 저장
                            documentRef.update("body_shape", bodyShape)
                                .addOnSuccessListener {
                                    // 결과 화면으로 전환
                                    btnResult = binding.activityHandAnalysisBtnResult
                                    btnResult.visibility = View.VISIBLE
                                    btnResult.setOnClickListener {
                                        intent = Intent(this, ResultActivity::class.java)
                                        intent.putExtra("Activity", "Hand")
                                        showBodyResultLayout(bodyShape, name)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("FaceAnalysis", "Error adding document", e)
                                }
                        } else
                            utils.showToast("사용자 정보가 데이터베이스에 존재하지 않습니다.")
                    }
                    .addOnFailureListener { exception ->
                        Log.w("signUp", "Error getting documents: ", exception)
                    }
            } else
                utils.showToast("이미지 분석에 실패했습니다.")
        }
    }

    // 체형 결과 레이아웃 설정
    private fun showBodyResultLayout(bodyShape: String, name: String) {
        isResultScreen = true
        when (bodyShape) {
            "Straight" -> {
                setContentView(R.layout.activity_result_straight)
                findViewById<TextView>(R.id.straight_tvUserName)?.text = name
            }

            "Natural" -> {
                setContentView(R.layout.activity_result_natural)
                findViewById<TextView>(R.id.natural_tvUserName)?.text = name
            }

            "Wave" -> {
                setContentView(R.layout.activity_result_wave)
                findViewById<TextView>(R.id.wave_tvUserName)?.text = name
            }

            else -> utils.showToast("잘못된 체형 결과입니다.")
        }
    }

    // ***** 추출한 LandMark를 이미지에 그리는 메소드 모음 ******
    // 1. initPaints() : 초기값 설정 메소드
    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(this, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL
    }

    // 2. setResults() : 변수 값 설정하는 메소드
    private fun setResults(
        handLandmarkerResults: HandLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = handLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE,
            RunningMode.VIDEO -> {
                min(ivImage.width * 1f / imageWidth, ivImage.height * 1f / imageHeight)
            }

            RunningMode.LIVE_STREAM -> {
                // PreviewView is in FILL_START mode. So we need to scale up the
                // landmarks to match with the size that the captured images will be
                // displayed.
                max(ivImage.width * 1f / imageWidth, ivImage.height * 1f / imageHeight)
            }
        }
    }

    // 3. draw() : 매개변수로 주어진 canvas에 실제 LandMark를 그리는 메소드
    private fun draw(canvas: Canvas) {
        results?.let { handLandmarkerResult ->
            for (landmark in handLandmarkerResult.landmarks()) {
                Log.i("landmark", "landmark = $landmark")
                // 1. 추출된 LandMark를 점으로 표시
                for (normalizedLandmark in landmark) {
                    Log.i("normalizedlandmark", "normalizedlandmark = $normalizedLandmark")
                    canvas.drawPoint(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        pointPaint
                    )
                    Log.i("norX", "norX = ${normalizedLandmark.x()}")
                }
                // 2. LandMark들끼리 선으로 연결
                HandLandmarker.HAND_CONNECTIONS.forEach {
                    canvas.drawLine(
                        landmark[it!!.start()]
                            .x() * imageWidth * scaleFactor,
                        landmark[it.start()]
                            .y() * imageHeight * scaleFactor,
                        landmark[it.end()]
                            .x() * imageWidth * scaleFactor,
                        landmark[it.end()]
                            .y() * imageHeight * scaleFactor,
                        linePaint
                    )
                }
            }
        }
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

    // 앱 종료
    private fun exitProgram() {
        moveTaskToBack(true)
        finishAndRemoveTask()
        exitProcess(0)
    }
}
