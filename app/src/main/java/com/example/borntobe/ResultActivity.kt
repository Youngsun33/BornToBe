package com.example.borntobe

import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.borntobe.databinding.ActivityResultBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var tvUserName: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvComment: TextView
    private lateinit var tvFeature: TextView
    private lateinit var tvTip: TextView
    private lateinit var iv: ImageView
    private lateinit var ivResult: ImageView
    private val utils: Utils = Utils(this)
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
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로가기 콜백 등록
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // Firestore 데이터베이스 초기화
        db = Firebase.firestore

        val dataStore = DataStoreModule(this)
        // 현재 사용자 정보 가져오기
        var id = "NULL"
        var name = "NULL"
        var pw = "NULL"
        lifecycleScope.launch {
            dataStore.userNameFlow.collect { userName ->
                name = userName
                Log.i("A_Result", "User Name: $name")
            }
        }
        lifecycleScope.launch {
            dataStore.userIDFlow.collect { userID ->
                id = userID
                Log.i("A_Result", "User ID: $id")
            }
        }
        lifecycleScope.launch {
            dataStore.userPWFlow.collect { userPW ->
                pw = userPW
                Log.i("A_Result", "User PW: $pw")
            }
        }

        // set userName
        tvUserName = binding.activityResultTvUserName
        tvUserName.text = name

        // set gradient
        tvResult = binding.activityResultTvResult
        tvResult.setTextColorAsLinearGradient(
            arrayOf(
                Color.parseColor("#EB00FF"),
                Color.parseColor("#0029FF")
            )
        )

        // set ImageView and TextView
        iv = binding.activityResultIvResult
        tvComment = binding.activityResultTvComment
        tvFeature = binding.activityResultTvFeature
        tvTip = binding.activityResultTvTip
        ivResult = binding.activityResultIvResult

        // 얼굴형 및 체형 결과 가져오기
        var face = "NULL"
        var body = "NULL"
        db.collection("users")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // 문서가 존재하면 업데이트
                    val document = documents.documents[0] // 첫 번째 문서 가져오기

                    face = document.get("face_shape").toString()
                    body = document.get("body_shape").toString()
                    Log.i("A_Result", "face, body = ${face}, ${body}")
                }
            }

        val activity = intent.getStringExtra("Activity")
        if (activity.equals("Face")) {
            // 얼굴형 및 체형 결과 가져오기

            // 얼굴형
            when (face) {
                "Oval" -> {
                    setContentView(R.layout.activity_result_oval)
                    findViewById<TextView>(R.id.oval_tvUserName).text = name
                }

                "Round" -> {
                    setContentView(R.layout.activity_result_round)
                    findViewById<TextView>(R.id.round_tvUserName).text = name
                }

                "Square" -> {
                    setContentView(R.layout.activity_result_square)
                    findViewById<TextView>(R.id.square_tvUserName).text = name
                }

                "Long" -> {
                    setContentView(R.layout.activity_result_long)
                    findViewById<TextView>(R.id.long_tvUserName).text = name
                }

                else -> utils.showToast("잘못된 결과입니다.")
            }
        } else if (activity.equals("Hand")) {

            // 체형
            when (body) {
                "Straight" -> {
                    setContentView(R.layout.activity_result_straight)
                    findViewById<TextView>(R.id.straight_tvUserName).text = name
                }

                "Natural" -> {
                    setContentView(R.layout.activity_result_natural)
                    findViewById<TextView>(R.id.natural_tvUserName).text = name
                }

                "Wave" -> {
                    setContentView(R.layout.activity_result_wave)
                    findViewById<TextView>(R.id.wave_tvUserName).text = name
                }

                else -> utils.showToast("잘못된 결과입니다.")
            }
        }
    }

    // gradient method
    private fun TextView.setTextColorAsLinearGradient(colors: Array<Int>) {
        if (colors.isEmpty()) {
            return
        }

        setTextColor(colors[0])
        this.paint.shader = LinearGradient(
            0f,
            0f,
            paint.measureText(this.text.toString()),
            this.textSize,
            colors.toIntArray(),
            arrayOf(0f, 1f).toFloatArray(),
            Shader.TileMode.CLAMP
        )
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