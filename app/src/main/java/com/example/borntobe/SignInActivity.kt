package com.example.borntobe

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.borntobe.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    // ViewBinding 사용
    private lateinit var binding: ActivitySignInBinding
    private lateinit var btnSignIn: Button
    private lateinit var btnSignUp: Button
    private lateinit var userID: EditText
    private lateinit var userPW: EditText
    private lateinit var utils: Utils
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        utils = Utils(this)

        // 로그인 버튼 : 사용자 입력 검사 & 사용자 정보 반환 & 메인 화면 전환
        btnSignIn = binding.activitySignInBtnSignIn
        userID = binding.activitySignInEtID
        userPW = binding.activitySignInEtPW
        btnSignIn.setOnClickListener {
            // 입력 검사
            val id = userID.text.toString()
            val password = userPW.text.toString()
            // isBlank() : 문자열 길이가 0 이거나, 공백(white space)으로만 이루어진 경우 true 리턴
            val isIDBlank = id.isBlank()
            val isPasswordBlank = password.isBlank()
            if (isIDBlank)
                utils.showToast("ID를 입력하세요.")
            else if (isPasswordBlank)
                utils.showToast("비밀번호를 입력하세요.")
            // 로그인 : 메인 화면 전환
            else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        // 회원 가입 버튼 : 회원 가입 화면 전환
        btnSignUp = binding.activitySignInBtnSignUp
        btnSignUp.setOnClickListener {
            btnSignUp.setTextColor(Color.WHITE)
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 화면 클릭 시, 키보드 숨기기
        binding.root.setOnClickListener {
            utils.hideKeyboard()
        }
    }
}