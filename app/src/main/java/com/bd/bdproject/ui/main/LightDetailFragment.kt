package com.bd.bdproject.ui.main

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.databinding.FragmentLightDetailBinding
import com.bd.bdproject.ui.main.adapter.TagAdapter
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.util.toBitDamDateFormat
import com.bd.bdproject.viewmodel.LightViewModel
import org.koin.android.ext.android.inject

class LightDetailFragment: Fragment() {

    private var _binding: FragmentLightDetailBinding? = null
    private val binding get() = _binding!!

    private val lightViewModel: LightViewModel by inject()
    private var tagAdapter: TagAdapter? = null

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLightDetailBinding.inflate(inflater, container, false).apply {
            observeLight()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lightViewModel.getLightWithTags(System.currentTimeMillis().timeToString())
        setTagRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeLight() {
        lightViewModel.lightWithTags.observe(requireActivity()) {
            binding.apply {
                tvDate.text = it.light.dateCode.toBitDamDateFormat()
                tvBrightness.text = it.light.bright.toString()
                gradientDrawable.colors = LightUtil.getDiagonalLight(it.light.bright * 2)
                layoutLightDetail.background = gradientDrawable

                // 여기에 밝기까지 다 넘겨버리기 (밝기비교를 여기서 할까 아니면 어댑터 안에서 할까)
                tagAdapter?.submitList(it.tags.toMutableList(), it.light.bright)
            }
        }
    }

    private fun setTagRecyclerView() {
        binding.apply {
            val layoutManager = LinearLayoutManager(requireActivity()).apply {
                orientation = RecyclerView.VERTICAL
            }
            tagAdapter = TagAdapter()

            rvTag.layoutManager = layoutManager
            rvTag.adapter = tagAdapter
        }
    }

}