package com.example.borntobe

import android.opengl.GLES20
import android.opengl.GLSurfaceView; // 올바른 임포트 문장
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        // 배경색 설정
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    }

    override fun onDrawFrame(gl: GL10) {
        // 화면을 지우기 위해 색상 버퍼를 클리어
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        // 뷰포트 설정
        GLES20.glViewport(0, 0, width, height)
    }
}
