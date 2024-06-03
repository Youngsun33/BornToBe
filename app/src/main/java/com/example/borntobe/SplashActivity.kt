package com.example.borntobe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.flow.map


class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT: Long = 3000 // 3초
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 3초 후 다음 화면
        Handler().postDelayed({
            var flag = false
            // DataStoreModule() : 자동 로그인을 위한 datastore 객체 생성
            val dataStore = DataStoreModule(this)
            val autoLoginState = dataStore.autoLoginState.map {
                flag = it
            }
            Log.i("datastore", "flag = $flag")
            Log.i("datastore", "autoLoginState = $autoLoginState")
            // 로그인 여부
            if (flag) {
                // 로그인 -> 메인 화면
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // 로그인x -> 온보딩 화면
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            // 현재 액티비티 종료
            finish()
        }, SPLASH_TIME_OUT)
    }
}