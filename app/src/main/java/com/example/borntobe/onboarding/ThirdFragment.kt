package com.example.borntobe.onboarding

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.borntobe.R
import androidx.navigation.fragment.findNavController


class ThirdFragment : Fragment() {

    private var _binding: ThirdFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ThirdFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)

        binding.txtPrevious.setOnClickListener {
            viewPager?.currentItem = 1
        }
        binding.txtStart.setOnClickListener {
            findNavController().navigate(R.id.action_onBoardingFragment_to_homeFragment)
            onBoardingFinished()
        }
    }

    private fun onBoardingFinished() {
        val prefs = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("finished", true).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ThirdFragment()
    }
}