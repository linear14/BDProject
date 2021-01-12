package com.bd.bdproject.ui.main

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.bd.bdproject.MainNavigationDirections
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.data.model.Tags
import com.bd.bdproject.databinding.FragmentLightDetailBinding
import com.bd.bdproject.ui.BaseFragment
import com.bd.bdproject.ui.MainActivity.Companion.LIGHT_DETAIL
import com.bd.bdproject.ui.main.adapter.TagAdapter
import com.bd.bdproject.util.ColorUtil.setEntireViewColor
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.util.toBitDamDateFormat
import com.bd.bdproject.viewmodel.LightViewModel
import com.bd.bdproject.viewmodel.main.AddViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import org.koin.android.ext.android.inject

class LightDetailFragment: BaseFragment() {

    private var _binding: FragmentLightDetailBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: AddViewModel by activityViewModels()
    private val lightViewModel: LightViewModel by inject()
    private var tagAdapter: TagAdapter? = null

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentLightDetailBinding.inflate(inflater, container, false).apply {

            actionEditBrightness.setOnClickListener {
                val light = lightViewModel.lightWithTags.value?.light
                sharedViewModel.previousPage.value = LIGHT_DETAIL

                val navDirection: NavDirections = LightDetailFragmentDirections
                    .actionLightDetailFragmentToAddLightFragment(light)
                Navigation.findNavController(it).navigate(navDirection)
            }
            actionEditTag.setOnClickListener {
                val light = lightViewModel.lightWithTags.value?.light
                val tags = Tags()
                for(i in lightViewModel.lightWithTags.value?.tags?: mutableListOf()) {
                    tags.add(i)
                }
                sharedViewModel.previousPage.value = LIGHT_DETAIL

                val navDirection: NavDirections = LightDetailFragmentDirections
                    .actionLightDetailFragmentToAddTagFragment(light, tags)
                Navigation.findNavController(it).navigate(navDirection)
            }
            actionEditMemo.setOnClickListener {
                val light = lightViewModel.lightWithTags.value?.light
                sharedViewModel.previousPage.value = LIGHT_DETAIL

                val navDirection: NavDirections = LightDetailFragmentDirections
                    .actionLightDetailFragmentToAddMemoFragment(light)
                Navigation.findNavController(it).navigate(navDirection)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments?.getString("dateCode").isNullOrEmpty()) {
            mainActivity.binding.btnDrawer.visibility = View.VISIBLE
            mainActivity.binding.btnBack.visibility = View.GONE
        } else {
            mainActivity.binding.btnDrawer.visibility = View.GONE
            mainActivity.binding.btnBack.visibility = View.VISIBLE
        }

        binding.apply {
            fabMore.setOnClickListener { controlBackgroundByFabState() }
            viewFilter.setOnClickListener { controlBackgroundByFabState() }
        }

        setTagRecyclerView()

    }

    override fun onResume() {
        super.onResume()

        setOnBackPressed()
        observeLight()

        arguments?.getString("dateCode")?.let {
            lightViewModel.getLightWithTags(it)
        } ?: lightViewModel.getLightWithTags(System.currentTimeMillis().timeToString())
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

                setEntireDetailFragmentColor(brightness)

                tvDate.text = dateCode.toBitDamDateFormat()
                tvBrightness.text = brightness.toString()
                tvMemo.text = memo?:""
                gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
                layoutLightDetail.background = gradientDrawable

                tagAdapter?.submitList(tags.toMutableList(), brightness)
            }
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
                    mainActivity.binding.apply {
                        btnDrawer.visibility = View.GONE
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }

                }
                View.VISIBLE -> {
                    viewFilter.visibility = View.GONE
                    layoutMore.visibility = View.GONE
                    mainActivity.binding.apply {
                        btnDrawer.visibility = View.VISIBLE
                        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }
                }
            }
        }
    }

    private fun setEntireDetailFragmentColor(brightness: Int) {
        binding.apply {
            setEntireViewColor(
                brightness,
                tvBrightness,
                tvDate,
                tvMemo,
                btnSpreadUpDown,
                mainActivity.binding.btnDrawer,
                mainActivity.binding.btnBack
            )
        }
    }

    private fun setOnBackPressed() {
        onBackPressedListener = object: OnBackPressedInFragment {
            override fun onBackPressed(): Boolean {
                return if(binding.viewFilter.visibility == View.VISIBLE) {
                    controlBackgroundByFabState()
                    true
                } else {
                    false
                }
            }
        }
    }

}