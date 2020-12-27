package com.bd.bdproject.ui.main

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.bd.bdproject.R
import com.bd.bdproject.databinding.FragmentLightDetailBinding
import com.bd.bdproject.ui.BaseFragment
import com.bd.bdproject.ui.MainActivity
import com.bd.bdproject.ui.main.adapter.TagAdapter
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.util.toBitDamDateFormat
import com.bd.bdproject.viewmodel.LightViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import org.koin.android.ext.android.inject

class LightDetailFragment: BaseFragment() {

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
            fabMore.setOnClickListener { controlBackgroundByFabState() }
            viewFilter.setOnClickListener { controlBackgroundByFabState() }
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
            val layoutManager = FlexboxLayoutManager(requireActivity()).apply {
                flexDirection = FlexDirection.COLUMN
            }
            tagAdapter = TagAdapter()

            rvTag.layoutManager = layoutManager
            rvTag.adapter = tagAdapter
        }
    }

    private fun controlBackgroundByFabState() {
        binding.apply {
            when(viewFilter.visibility) {
                View.GONE -> {
                    viewFilter.visibility = View.VISIBLE
                    layoutMore.visibility = View.VISIBLE
                    (activity as MainActivity).binding.apply {
                        btnDrawer.visibility = View.GONE
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }

                }
                View.VISIBLE -> {
                    viewFilter.visibility = View.GONE
                    layoutMore.visibility = View.GONE
                    (activity as MainActivity).binding.apply {
                        btnDrawer.visibility = View.VISIBLE
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }
                }
            }
        }
    }

}