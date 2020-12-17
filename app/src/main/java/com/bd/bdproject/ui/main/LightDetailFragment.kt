package com.bd.bdproject.ui.main

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.bd.bdproject.R
import com.bd.bdproject.databinding.FragmentLightDetailBinding
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.viewmodel.LightViewModel
import com.google.android.material.chip.Chip
import org.koin.android.ext.android.inject

class LightDetailFragment: Fragment() {

    private var _binding: FragmentLightDetailBinding? = null
    private val binding get() = _binding!!

    private val lightViewModel: LightViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLightDetailBinding.inflate(inflater, container, false).apply {
            lightViewModel.getLightWithTags(System.currentTimeMillis().timeToString())
            observeLight()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun observeLight() {
        lightViewModel.lightWithTags.observe(requireActivity()) {
            binding.apply {
                tvBrightness.text = it.light.bright.toString()

                for(i in it.tags) {
                    val nameWithHash = "# ${i.name}"
                    Chip(requireActivity()).apply {
                        text = nameWithHash
                        setTextAppearanceResource(R.style.ChipTextStyle)
                    }.also { chip ->
                        chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(chip.context, android.R.color.transparent))
                        chip.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(chip.context, android.R.color.white))
                        chip.chipStrokeWidth = 1f
                        flexBoxTag.addView(chip)
                        (chip.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(
                            left = chip.context.resources.getDimensionPixelSize(R.dimen.chip_margin),
                            right = chip.context.resources.getDimensionPixelSize(R.dimen.chip_margin)
                        )
                    }
                }
            }
        }
    }

}