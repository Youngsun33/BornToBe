package com.example.borntobe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.borntobe.databinding.ActivityOnboardingBinding
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlin.system.exitProcess

class OnboardingActivity : AppCompatActivity() {
    // ViewBinding 사용
    private lateinit var binding: ActivityOnboardingBinding
    // 위젯 변수
    private lateinit var viewPager: ViewPager2
    private lateinit var dotsIndicator: DotsIndicator
    private lateinit var ll: LinearLayout
    private lateinit var btnStart: ImageButton
    private lateinit var btnSignUp: Button
    private lateinit var btnSignIn: Button
    private lateinit var utils: Utils

    // onBackPressedCallback : 뒤로 가기 동작을 정의하는 callback 메소드
    private var backKeyPressedTime = 0L
    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(true) {
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
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // utils 객체 생성
        utils = Utils(this)
        // 뒤로 가기 버튼 동작 정의
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        // ViewPager 연결
        viewPager = binding.activityOnboardingViewPager
        // ViewPager 에 들어갈 아이템 배열
        val imageList = arrayListOf(
            R.drawable.ic_onboarding_01,
            R.drawable.ic_onboarding_02,
            R.drawable.ic_onboarding_03,
            R.drawable.ic_onboarding_04,
            R.drawable.ic_onboarding_05
        )
        val textList = arrayListOf(
            R.string.activity_onboarding_tv01,
            R.string.activity_onboarding_tv02,
            R.string.activity_onboarding_tv03,
            R.string.activity_onboarding_tv04,
            R.string.activity_onboarding_tv05
        )
        // Create Adapter
        viewPager.adapter = ViewPagerAdapter(imageList, textList)
        // 방향을 가로로 설정
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        // viewPager 와 dotsIndicator 연결
        dotsIndicator = binding.activityOnboardingDotsIndicator
        dotsIndicator.attachTo(viewPager)
        // 사용자가 마지막 페이지까지 확인하면 화면 이동 버튼 활성화
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == 4) {
                    ll = binding.activityOnboardingLl
                    ll.visibility = View.VISIBLE
                }
            }
        })

        // btnStart : 회원 가입 화면 이동 버튼
        btnStart = binding.activityOnboardingBtnStart
        btnStart.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        // btnSignUp : 회원 가입 화면 이동
        btnSignUp = binding.activityOnboardingBtnSignUp
        btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        // btnSignIn : 로그인 화면 이동
        btnSignIn = binding.activityOnboardingBtnSignIn
        btnSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
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

