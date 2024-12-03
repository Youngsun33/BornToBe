package com.example.borntobe

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
import com.example.borntobe.databinding.ActivityResultBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
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
    private val utils: Utils = Utils(this)

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
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // set userName
        tvUserName = binding.activityResultTvUserName
        tvUserName.text = intent.getStringExtra("userName")

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

        // **** 이전 화면에서 넘겨받은 값 가져오는 부분 *****
        // getStringExtra("Key 값") : Key 값은 이전 화면 (FaceAnalysis, HandAnalysis)에서 준 값
        // 사용자 이름 key 값 : userName
        // 체형 결과 key 값 : bodyShape
        // 얼굴형 key 값 : faceShape --> intent.getStringExtra("faceShape") 이런 식으로 활용
        // faceShape의 값으로는 Oval, Oblong, Round, Square 있음
        // if (faceShape.equal("Oval")) {Oval 결과 화면 레이아웃 띄우는 코드} 이런 식으로 작성하면 될 듯
        val bodyShape = intent.getStringExtra("bodyShape")
        when (bodyShape) {
            "Straight" -> {
                iv.setImageResource(R.drawable.ic_result_straight)
                tvComment.setText(R.string.activity_result_straight_comment)
                tvFeature.setText(R.string.activity_result_straight_feature)
                tvTip.setText(R.string.activity_result_straight_tip)
            }

            "Natural" -> {
                iv.setImageResource(R.drawable.ic_result_natural)
                tvComment.setText(R.string.activity_result_natural_comment)
                tvFeature.setText(R.string.activity_result_natural_feature)
                tvTip.setText(R.string.activity_result_natural_tip)
            }

            else -> {
                iv.setImageResource(R.drawable.ic_result_wave)
                tvComment.setText(R.string.activity_result_wave_comment)
                tvFeature.setText(R.string.activity_result_wave_feature)
                tvTip.setText(R.string.activity_result_wave_tip)
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