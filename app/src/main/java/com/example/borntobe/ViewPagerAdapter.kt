package com.example.borntobe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.borntobe.databinding.OnboardingItemBinding

class ViewPagerAdapter(private val imageList: ArrayList<Int>, private val textList: ArrayList<Int>) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {
    private lateinit var binding: OnboardingItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = OnboardingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = imageList.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imageList[position], textList[position])
    }

    inner class ViewHolder(private val binding: OnboardingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Int, text: Int) {
            binding.onboardingItemIv.setImageResource(image)
            binding.onboardingItemTv.setText(text)
        }
    }
}