package com.example.borntobe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SplashActivity : AppCompatActivity() {
    private val SPLASH_TIME_OUT: Long = 3000 // 3초

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // DataStoreModule() : 자동 로그인을 위한 datastore 객체 생성
        val dataStore = DataStoreModule(this)
        var autoLoginState = "false"
        // DataStore 파일에서 자동 로그인 사용 설정 여부 가져오기
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.autoLoginState.collectLatest {
                autoLoginState = it
            }
        }
        // 3초 후 다음 화면
        Handler().postDelayed({
            // 자동 로그인 사용 여부
            if (autoLoginState == "true") {
                // 자동 로그인 설정 O -> 메인 화면
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                // 자동 로그인 설정 X -> 온보딩 화면
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
        }, SPLASH_TIME_OUT)
    }
}