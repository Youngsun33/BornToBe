package com.example.borntobe

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.borntobe.databinding.ActivityHandAnalysisBinding
import com.google.mediapipe.tasks.vision.core.RunningMode

class HandAnalysisActivity : AppCompatActivity() {
    // ViewBinding 사용
    private lateinit var binding: ActivityHandAnalysisBinding
    private lateinit var ivImage: ImageView
    private lateinit var btnImageUpload: Button
    private lateinit var btnAnalysis: Button
    private lateinit var bitmap: Bitmap
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var overlayView: OverlayView
    private val utils: Utils = Utils(this)

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
                ivImage.setImageURI(uri)
                Log.i("PhotoPicker", "Image Upload Success")
                utils.showToast("이미지가 업로드 되었습니다.")
                // uri를 bitmap으로 변환
                val source = ImageDecoder.createSource(this.contentResolver, uri)
                bitmap = ImageDecoder.decodeBitmap(source)
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

        // 분석 버튼 : 업로드 된 손 사진에서 land-mark 추출 & 각 부위별 길이 측정
        btnAnalysis = binding.activityHandAnalysisBtnAnalysis
        btnAnalysis.setOnClickListener {
            // 손 분석을 위한 HandLandMarkerHelper 객체 생성
            handLandmarkerHelper = HandLandmarkerHelper(
                0.5F,
                0.5F,
                0.5F,
                1,
                0,
                RunningMode.IMAGE,
                this)
            // 손 분석 결과로 추출된 LandMark 반환
            val handLandMarkerResult = handLandmarkerHelper.detectImage(bitmap)
            if (handLandMarkerResult != null) {
//                val canvas = Canvas(bitmap)
//                overlayView = OverlayView(this, null)
//                overlayView.clear()
//                overlayView.setResults(handLandMarkerResult, bitmap.height, bitmap.width)
//                overlayView.draw(canvas)
                utils.showToast("반환")
            } else
                utils.showToast("분석을 수행할 수 없습니다.")
        }
    }
}