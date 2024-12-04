package com.example.borntobe

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.borntobe.databinding.ActivitySignUpBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

/** SignUp 클래스 :
 *  신규 사용자를 위한 회원 가입 화면 */
class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var db: FirebaseFirestore
    private var isExistID = false
    private var isBtnIDCheckClick = false

    // 위젯 변수들
    private lateinit var userID: EditText
    private lateinit var userPW: EditText
    private lateinit var userPWCheck: EditText
    private lateinit var userName: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnIDCheck: Button
    private lateinit var btnCancel: ImageButton
    private lateinit var utils: Utils

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
        binding = ActivitySignUpBinding.inflate(layoutInflater)
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
            if (isIDBlank) {
                utils.showToast("ID를 입력해주세요.")
            } else if (isPWBlank) {
                utils.showToast("비밀번호를 입력해주세요.")
            } else if (isPWChkBlank) {
                utils.showToast("비밀번호를 다시 한 번 입력해주세요.")
            } else if (isNameBlank) {
                utils.showToast("닉네임을 입력해주세요.")
            } else if (!utils.isRegularID(id, userID)) {
                utils.showToast("ID를 확인해주세요. \n ID는 6자 이상의 영문 혹은 영문과 숫자의 조합이어야 합니다.")
            } else if (!utils.isRegularPW(pw, userPW)) {
                utils.showToast("비밀번호는 영문, 숫자, 특수문자(!@#$%?_) 조합의 8~16자이어야 합니다.")
            } else if (pw != pwChk) {
                userPWCheck.setBackgroundResource(R.drawable.bg_round_square_stroke_red)
                utils.showToast("입력하신 비밀번호가 서로 다릅니다. \n 비밀번호를 한 번 더 확인해주세요.")
            } else if (!isBtnIDCheckClick) {
                utils.showToast("ID 중복 검사를 해주세요.")
            } else if (isExistID) {
                utils.showToast("이미 존재하는 ID 입니다.")
                // 회원 가입
            } else {
                // DB에 사용자 정보 저장
                // Add a new document with a generated id.
                val data = hashMapOf(
                    "id" to id,
                    "pw" to pw,
                    "name" to name,
                    "face_shape" to null,
                    "body_shape" to null
                )

                db.collection("users")
                    .add(data)
                    .addOnSuccessListener { documentReference ->
                        Log.d("signUp", "DocumentSnapshot written with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("signUp", "Error adding document", e)
                    }

                // 내부 DB에 사용자 정보 저장
                val dataStore = DataStoreModule(applicationContext)
                lifecycleScope.launch {
                    dataStore.saveUserID(id)
                    dataStore.saveUserPW(pw)
                    dataStore.saveUserName(name)
                    dataStore.saveAutoLoginState("true")
                }
                Log.i("A : SignUP", id)
                Log.i("A : SignUP", pw)
                Log.i("A : SignUP", name)

                // 로그인 : 메인 화면 전환
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // ID 중복 검사 버튼 : 사용자 ID 중복 검사
        btnIDCheck = binding.activitySignUpBtnIDCheck
        btnIDCheck.setOnClickListener {
            isBtnIDCheckClick = true
            val id = userID.text.toString()
            if (id.isBlank()) {
                utils.showToast("ID를 입력해주세요.")
            } else {
                // 사용자가 입력한 ID와 중복 되는 ID가 존재하는지 검사
                db.collection("users")
                    .whereEqualTo("id", id)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            isExistID = true
                            isBtnIDCheckClick = false
                            utils.showToast("이미 존재하는 ID 입니다.")
                        } else {
                            isExistID = false
                            utils.showToast("사용 가능한 ID 입니다.")
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

        // PW Check EditText : TextWatcher 연결하여 실시간 비밀번호 일치 확인
        userPWCheck.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                // 텍스트 변경될 때마다 호출 : 실시간으로 입력 값 검사 가능
                val isMatch = (userPW.text.toString() == userPWCheck.text.toString())
                if (isMatch)
                    userPWCheck.backgroundTintList = getColorStateList(R.color.gray)
                else
                    userPWCheck.backgroundTintList = getColorStateList(R.color.red)
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

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