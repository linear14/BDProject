package com.bd.bdproject.ui.main

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.R
import com.bd.bdproject.databinding.FragmentLightDetailBinding
import com.bd.bdproject.ui.main.adapter.TagAdapter
import com.bd.bdproject.ui.main.adapter.TagAdapter.Companion.ORIENTATION_VERTICAL
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
                val dateCode = it.light.dateCode
                val brightness = it.light.bright
                val memo = it.light.memo
                val tags = it.tags

                when(brightness) {
                    in 0 until 80 -> { setEntireTextColor(R.color.white, tvDate, tvBrightness, tvMemo) }
                    else -> { setEntireTextColor(R.color.black, tvDate, tvBrightness, tvMemo) }
                }

                tvDate.text = dateCode.toBitDamDateFormat()
                tvBrightness.text = brightness.toString()
                tvMemo.text = memo?:""
                gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
                layoutLightDetail.background = gradientDrawable

                tagAdapter?.submitList(tags.toMutableList(), brightness)
            }
        }
    }

    private fun setEntireTextColor(color: Int, vararg view: TextView) {
        for(item in view) {
            item.setTextColor(ContextCompat.getColor(requireActivity(), color))
        }
    }

    private fun setTagRecyclerView() {
        binding.apply {
            val layoutManager = LinearLayoutManager(requireActivity()).apply {
                orientation = RecyclerView.VERTICAL
            }
            tagAdapter = TagAdapter(ORIENTATION_VERTICAL)

            rvTag.layoutManager = layoutManager
            rvTag.adapter = tagAdapter
        }
    }

}