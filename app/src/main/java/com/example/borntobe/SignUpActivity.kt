package com.example.borntobe

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.borntobe.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase

/** 회원 가입 화면
 * 개발 담당 : 박은진 */
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var db: Firebase

    // 위젯 변수들
    private lateinit var userID: EditText
    private lateinit var userPW: EditText
    private lateinit var userPWCheck: EditText
    private lateinit var userName: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnCancel: ImageButton
    private lateinit var utils: Utils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        enableEdgeToEdge()
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // utils 객체 생성
        utils = Utils(this)

        // 취소 버튼 : 로그인 화면으로 돌아감
        btnCancel = binding.activitySignUpBtnCancel
        btnCancel.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        // 회원 가입 버튼 : 사용자 입력 유효성 검사 & 사용자 정보 DB 저장 & 메인 화면 이동
        btnSignUp = binding.activitySignUpBtnSignUp
        userID = binding.activitySignUpEtID
        userPW = binding.activitySignUpEtPW
        userPWCheck = binding.activitySignUpEtPWCheck
        userName = binding.activitySignUpEtUserName
        btnSignUp.setOnClickListener {
            // 입력 검사
            val id = userID.text.toString()
            val pw = userPW.text.toString()
            val pwChk = userPWCheck.text.toString()
            val name = userName.text.toString()
            // isBlank() : 문자열 길이가 0 이거나, 공백(white space)으로만 이루어진 경우 true 리턴
            val isIDBlank = id.isBlank()
            val isPWBlank = pw.isBlank()
            val isPWChkBlank = pwChk.isBlank()
            val isNameBlank = name.isBlank()
            // 입력 유효성 검사
            if (isIDBlank)
                utils.showToast("ID를 입력해주세요.")
            else if (isPWBlank)
                utils.showToast("비밀번호를 입력해주세요.")
            else if (isPWChkBlank)
                utils.showToast("비밀번호를 다시 한 번 입력해주세요.")
            else if (isNameBlank)
                utils.showToast("닉네임을 입력해주세요.")
            else if (!utils.isRegularID(id, userID))
                utils.showToast("ID를 확인해주세요. ID는 6자 이상의 영문 혹은 영문과 숫자의 조합이어야 합니다.")
            else if (!utils.isRegularPW(pw, userPW))
                utils.showToast("비밀번호는 영문, 숫자, 특수문자(!@#$%?_) 조합의 8~16자이어야 합니다.")
            else if (pw != pwChk) {
                userPWCheck.setBackgroundResource(R.drawable.bg_round_square_stroke_red)
                utils.showToast("입력하신 비밀번호가 서로 다릅니다. 비밀번호를 한 번 더 확인해주세요.")
            }
            // 회원 가입
            else {
                // DB에 사용자 정보 저장

                // 로그인 : 메인 화면 전환
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        // ID EditText : TextWatcher 연결하여 실시간 유효성 검사
        userID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 텍스트 변경될 때마다 호출 : 실시간으로 입력 값 검사 가능
                utils.isRegularID(userID.text.toString().trim(), userID)
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        // PW EditText : TextWatcher 연결하여 실시간 유효성 검사
        userPW.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 텍스트 변경될 때마다 호출 : 실시간으로 입력 값 검사 가능
                utils.isRegularPW(userPW.text.toString().trim(), userPW)
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        // PW Check EditText : TextWatcher 연결하여 실시간 비밀번호 일치 확인
        userPWCheck.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 텍스트 변경될 때마다 호출 : 실시간으로 입력 값 검사 가능
                val isMatch = (userPW.text.toString() == userPWCheck.text.toString())
                if (isMatch)
                    userPWCheck.setBackgroundResource(R.drawable.bg_round_square_stroke_gray)
                else
                    userPWCheck.setBackgroundResource(R.drawable.bg_round_square_stroke_red)
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        // 화면 클릭 시, 키보드 숨기기
        binding.root.setOnClickListener {
            utils.hideKeyboard()
        }
    }
}