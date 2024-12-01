package com.example.borntobe

import android.app.Dialog
import android.content.Intent
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
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.borntobe.databinding.ActivityHandAnalysisBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.system.exitProcess

class HandAnalysisActivity : AppCompatActivity() {
    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
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
    private lateinit var btnAnalysis: ImageButton
    private lateinit var btnResult: Button
    private lateinit var image: Bitmap
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var dialog: Dialog
    private val utils: Utils = Utils(this)
    private lateinit var db: FirebaseFirestore

    // onBackPressedCallback : 뒤로 가기 동작을 정의하는 callback 메소드
    private var backKeyPressedTime = 0L
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 사용자가 2초 이내에 뒤로 가기 버튼을 한 번 더 클릭하면 화면 종료
                if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                    backKeyPressedTime = System.currentTimeMillis()
                    utils.showToast("뒤로가기를 한번 더 누르면 종료됩니다.")
                } else {
                    exitProgram()
                }
            }
        }

    // isPhotoPickerAvailable : 사진 선택 도구가 사용 가능한 기기 인지 확인
    private val isPhotoPickerAvailable =
        ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)

    // pickMedia : launch() 메소드로 PhotoPicker를 열었다가
    // 사용자가 선택한 사진을 가공해야 하므로 registerForActivityResult() 메소드를 활용
    // 콜백 함수로 구현
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the photo picker.
            // 1. 사용자가 이미지 선택 : 이미지 uri 반환
            // 2. 사용자가 이미지 선택하지 않고 photo picker 닫음 : null 반환
            if (uri != null) {
                Log.i("PhotoPicker", "Selected URI: $uri")
                // 이미지뷰에 가져온 이미지 설정
                ivImage.scaleType = ImageView.ScaleType.CENTER_CROP
                ivImage.setImageURI(uri)
                Log.i("PhotoPicker", "Image Upload Success")
                utils.showToast("이미지가 업로드 되었습니다.")
                // uri를 bitmap으로 변환
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source)
                image = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                Log.i("PhotoPicker", "No media selected")
                utils.showToast("이미지를 선택하세요.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHandAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // db 객체 생성
        db = Firebase.firestore
        // 뒤로 가기 버튼 동작 정의
        //onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // 다이얼로그 설정
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_caution)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCanceledOnTouchOutside(true)
        showDialog()

        // 이미지 업로드 버튼 : 갤러리에서 사진을 가져와 ImageView에 띄움
        ivImage = binding.activityHandAnalysisIvImage
        btnImageUpload = binding.activityHandAnalysisBtnImageUpload
        btnImageUpload.setOnClickListener {
            // photo picker 사용 가능한 기기라면
            if (isPhotoPickerAvailable) {
                // Launch the photo picker and let the user choose only images.
                // photo picker를 열고, 사용자가 오직 이미지만 선택하도록 함.
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } else
                Log.i("PhotoPicker", "PhotoPicker False")
        }

        // 손 분석을 위한 HandLandMarkerHelper 객체 생성
        handLandmarkerHelper = HandLandmarkerHelper(context = this)

        // 분석 버튼 : 업로드 된 손 사진에서 land-mark 추출 & 각 부위별 길이 측정
        btnAnalysis = binding.activityHandAnalysisBtnAnalysis
        btnAnalysis.setOnClickListener {
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
                utils.showToast("결과 사진 업로드 완료. 체형은 $bodyShape")
                // 현재 사용자 ID 가져오기
                val dataStore = DataStoreModule(this)
                val userID = dataStore.userIDFlow.toString()
                // 현재 사용자가 DB에 존재하는지 검사
                var isNotExistID = false
                db.collection("users")
                    .whereEqualTo("id", userID)
                    .get()
                    .addOnSuccessListener { documents ->
                        isNotExistID = documents.isEmpty
                    }
                    .addOnFailureListener { exception ->
                        Log.w("signUp", "Error getting documents: ", exception)
                    }
                if (!isNotExistID) {
                    // DB에 사용자 체형 결과 저장
                    val data = hashMapOf(
                        "body_shape" to bodyShape
                    )
                    db.collection("users")
                        .add(data)
                        .addOnSuccessListener { documentReference ->
                            Log.d(
                                "HandAnalysis",
                                "DocumentSnapshot written with ID: ${documentReference.id}"
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w("HandAnalysis", "Error adding document", e)
                        }
                    // 결과 화면으로 전환
                    btnResult = binding.activityHandAnalysisBtnResult
                    btnResult.visibility = View.VISIBLE
                    btnResult.setOnClickListener {
                        intent = Intent(this, ResultActivity::class.java)
                        var userName = "userName"
                        CoroutineScope(Dispatchers.IO).launch {
                            dataStore.userNameFlow.collectLatest {
                                userName = it
                            }
                        }
                        Thread.sleep(1000)
                        intent.putExtra("userName", userName)
                        intent.putExtra("bodyShape", bodyShape)
                        startActivity(intent)
                    }
                } else
                    utils.showToast("사용자 정보가 데이터베이스에 존재하지 않습니다.")
            } else
                utils.showToast("분석을 수행할 수 없습니다.")
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

    // 다이얼로그 띄워주는 메소드
    private fun showDialog() {
        dialog.show()

        // 확인 버튼
        dialog.findViewById<Button>(R.id.dialog_caution_btnConfirm).setOnClickListener {
            dialog.dismiss()
        }
    }

    // exitProgram() : App을 완전히 종료하는 메소드
    private fun exitProgram() {
        // 태스크를 백그라운드로 이동
        moveTaskToBack(true)
        // 액티비티 종료 + 태스크 리스트에서 지우기
        finishAndRemoveTask()
        // App Process 종료
        exitProcess(0)
    }
}