package com.example.borntobe.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.borntobe.R

class SecondFragment : Fragment() {

    private var _binding: SecondFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SecondFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.viewPager)

        binding.txtPrevious.setOnClickListener {
            viewPager?.currentItem = 0
        }
        binding.txtNext.setOnClickListener {
            viewPager?.currentItem = 2
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SecondFragment()
    }

}