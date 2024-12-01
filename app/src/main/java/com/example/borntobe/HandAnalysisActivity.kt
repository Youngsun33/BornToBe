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
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.borntobe.databinding.ActivityHandAnalysisBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
    private lateinit var db: FirebaseFirestore

    // onBackPressedCallback : 뒤로 가기 동작을 정의하는 callback 메소드
    private var backKeyPressedTime = 0L
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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

        // 시스템 바 영역 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firestore 데이터베이스 초기화
        db = Firebase.firestore

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

        // 분석 버튼 클릭 리스너 추가
        btnAnalysis.setOnClickListener {
            utils.showToast("분석 기능은 구현 중입니다.")
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
