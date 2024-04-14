package com.example.borntobe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth


class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 3000 // 3초

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 3초 후 다음 화면
        Handler().postDelayed({
            // 로그인 여부
            if (isUserLoggedIn()) {
                // 로그인 -> 메인 화면
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // 로그인x -> 온보딩 화면
                startActivity(Intent(this, MainActivity::class.java)) //OnboardingActivity 없어서 에러-> 일단MainActivity넣어놓음
            }
            // 현재 액티비티 종료
            finish()
        }, SPLASH_TIME_OUT)
    }

    private fun isUserLoggedIn(): Boolean {
        // FirebaseAuth를 사용
        val currentUser = FirebaseAuth.getInstance().currentUser
        // 사용자 로그인 여부 확인
        return currentUser != null
    }
}