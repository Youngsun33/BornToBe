package com.example.borntobe

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.borntobe.databinding.ActivitySignInBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlin.system.exitProcess

class SignInActivity : AppCompatActivity() {
    // ViewBinding 사용
    private lateinit var binding: ActivitySignInBinding
    private lateinit var btnSignIn: Button
    private lateinit var btnSignUp: Button
    private lateinit var userID: EditText
    private lateinit var userPW: EditText
    private lateinit var utils: Utils

    // db 변수
    private lateinit var db: FirebaseFirestore

    // onBackPressedCallback : 뒤로 가기 동작을 정의하는 callback 메소드
    private var backKeyPressedTime = 0L
    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 사용자가 2초 이내에 뒤로 가기 버튼을 한 번 더 클릭하면 화면 종료
                if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                    backKeyPressedTime = System.currentTimeMillis()
                    utils.showToast("뒤로가기를 한번 더 누르면 종료됩니다.")
                } else {
                    exitProgram()
                }
            }
        }

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
        // utils 객체 생성
        utils = Utils(this)
        // db 객체 생성
        db = Firebase.firestore
        // 뒤로 가기 버튼 동작 정의
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // 로그인 버튼 : 사용자 입력 유효성 검사 & 사용자 정보 반환 & 메인 화면 전환
        btnSignIn = binding.activitySignInBtnSignIn
        userID = binding.activitySignInEtID
        userPW = binding.activitySignInEtPW
        btnSignIn.setOnClickListener {
            // 입력 검사
            val id = userID.text.toString()
            val pw = userPW.text.toString()
            // isBlank() : 문자열 길이가 0 이거나, 공백(white space)으로만 이루어진 경우 true 리턴
            val isIDBlank = id.isBlank()
            val isPWBlank = pw.isBlank()
            if (isIDBlank)
                utils.showToast("ID를 입력해주세요.")
            else if (isPWBlank)
                utils.showToast("비밀번호를 입력해주세요.")
            else if (!utils.isRegularID(id, userID))
                utils.showToast("ID를 확인해주세요. \n ID는 6자 이상의 영문 혹은 영문과 숫자의 조합이어야 합니다.")
            else if (!utils.isRegularPW(pw, userPW))
                utils.showToast("비밀번호를 확인해주세요.")
            else {
                // 사용자가 입력한 ID가 존재하는지 검사
                var isNotExistID = false
                db.collection("users")
                    .whereEqualTo("id", id)
                    .get()
                    .addOnSuccessListener { documents ->
                        isNotExistID = documents.isEmpty
                    }
                    .addOnFailureListener { exception ->
                        Log.w("signUp", "Error getting documents: ", exception)
                    }
                // 사용자가 입력한 PW가 존재하는지 검사
                db.collection("users")
                    .whereEqualTo("id", id)
                    .whereEqualTo("pw", pw)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (isNotExistID)
                            utils.showToast("해당하는 ID가 존재하지 않습니다.")
                        else if (documents.isEmpty)
                            utils.showToast("아이디 또는 비밀번호가 일치하지 않습니다.")
                        else {
                            // 로그인 : 메인 화면 전환
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("signUp", "Error getting documents: ", exception)
                    }
            }
        }

        // ID EditText : TextWatcher 연결하여 실시간 유효성 검사
        userID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 텍스트 변경될 때마다 호출 : 실시간으로 입력 값 검사 가능
                if (userID.text.isNotEmpty())
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
                if (userPW.text.isNotEmpty())
                    utils.isRegularPW(userPW.text.toString().trim(), userPW)
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

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

    // exitProgram() : App을 완전히 종료하는 메소드
    private fun exitProgram() {
        // 태스크를 백그라운드로 이동
        moveTaskToBack(true)
        // 액티비티 종료 + 태스크 리스트에서 지우기
        finishAndRemoveTask()
        // App Process 종료
        exitProcess(0)
    }
}