package com.example.borntobe

import android.os.Bundle
import android.content.Intent
import android.opengl.GLSurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate


class MainActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: GLSurfaceView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 앱의 다크 모드를 비활성화합니다.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // 'GLSurfaceView' 레이아웃 찾고 설정
        glSurfaceView = findViewById(R.id.glSurfaceView)
        glSurfaceView.apply {
            // OpenGL ES 버전을 2로 설정합니다.
            setEGLContextClientVersion(2)
            // 렌더러로 'MyGLRenderer'를 설정합니다.
            setRenderer(MyGLRenderer())
        }

        // "얼굴형 분석" 버튼에 클릭 리스너를 설정합니다.
        findViewById<Button>(R.id.buttonAnalyzeFace).setOnClickListener {
            val intent = Intent(this, FaceAnalysisActivity::class.java)
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