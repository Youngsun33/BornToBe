package com.example.borntobe

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.borntobe.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    // ViewBinding 설정
    private lateinit var binding: ActivityMainBinding

    // 위젯 변수
    private lateinit var ivAnalysis: ImageView
    private lateinit var dialog: Dialog
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
                    finish()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 뒤로 가기 버튼 동작 정의
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // 다이얼로그 설정
        dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_analysis)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.setCanceledOnTouchOutside(true)

        // 분석하기 이미지뷰 클릭 리스너
        ivAnalysis = binding.activityMainIvAnalysis
        ivAnalysis.setOnClickListener {
            showDialog()
        }
    }

    // 다이얼로그 띄워주는 메소드
    private fun showDialog() {
        dialog.show()

        // 체형 분석 버튼 
        dialog.findViewById<ImageView>(R.id.activity_main_ivAnalysisHand).setOnClickListener {
            // 체형 분석 화면 전환
            val intent = Intent(this, HandAnalysisActivity::class.java)
            startActivity(intent)
        }

        // 얼굴 분석 버튼
        dialog.findViewById<ImageView>(R.id.activity_main_ivAnalysisFace).setOnClickListener {
            // 얼굴 분석 화면 전환
            val intent = Intent(this, FaceAnalysisActivity::class.java)
            startActivity(intent)
        }
    }
}