package com.example.borntobe

import android.os.Bundle
import android.content.Intent
import android.opengl.GLSurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.borntobe.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    // ViewBinding 설정
    private lateinit var binding: ActivityMainBinding
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var btnAnalysisFace: Button
    private lateinit var btnAnalysisHand: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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