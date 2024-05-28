package com.example.borntobe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.borntobe.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    // ViewBinding 사용
    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var viewPager: ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            // Paging 완료되면 호출
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("ViewPagerFragment", "Page ${position+1}")
            }
        })
    }
}

