package com.example.borntobe

import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.borntobe.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    // ViewBinding 설정
    private lateinit var binding: ActivityMainBinding
    // 위젯 변수
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var btnAnalysisFace: Button
    private lateinit var btnAnalysisHand: Button
    private val utils: Utils = Utils(this)

    // onBackPressedCallback : 뒤로 가기 동작을 정의하는 callback 메소드
    private var backKeyPressedTime = 0L
    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // 사용자가 2초 이내에 뒤로 가기 버튼을 한 번 더 클릭하면 화면 종료
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis()
                utils.showToast("뒤로가기를 한번 더 누르면 종료됩니다.")
            } else {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로 가기 버튼 동작 정의
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // 'GLSurfaceView' 레이아웃 찾고 설정
        glSurfaceView = binding.activityMainGlSurfaceView
        glSurfaceView.apply {
            // OpenGL ES 버전을 2로 설정합니다.
            setEGLContextClientVersion(2)
            // 렌더러로 'MyGLRenderer'를 설정합니다.
            setRenderer(MyGLRenderer())
        }

        // "얼굴형 분석" 버튼에 클릭 리스너를 설정합니다.
        btnAnalysisFace = binding.activityMainBtnAnalysisFace
        btnAnalysisFace.setOnClickListener {
            // 얼굴 분석 화면으로 전환
            val intent = Intent(this, FaceAnalysisActivity::class.java)
            startActivity(intent)
        }

        // 손 분석 버튼 : HandAnalysisActivity로 이동
        btnAnalysisHand = binding.activityMainBtnAnalysisHand
        btnAnalysisHand.setOnClickListener {
            val intent = Intent(this, HandAnalysisActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // 'GLSurfaceView'를 다시 시작합니다.
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        // 'GLSurfaceView'를 일시 중지합니다.
        glSurfaceView.onPause()
    }
}