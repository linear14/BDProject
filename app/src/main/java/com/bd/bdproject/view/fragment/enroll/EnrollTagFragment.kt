package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.util.Constant.CONTROL_MEMO
import com.bd.bdproject.util.Constant.CONTROL_TAG
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.view.fragment.ControlTagFragment
import com.bd.bdproject.viewmodel.AddViewModel
import com.bd.bdproject.viewmodel.common.TagViewModel
import org.koin.android.ext.android.inject

open class EnrollTagFragment: ControlTagFragment() {

    private val tagViewModel: TagViewModel by inject()
    private val sharedViewModel: AddViewModel by activityViewModels()

    private val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.actionNext.setOnClickListener {
            if(!isChangingFragment) {
                isChangingFragment = true
                saveTags()
                goToFragmentEnrollMemo(it)
            }
        }

    }

    override fun onResume() {
        super.onResume()

        initBackground()
        showUi()
        observeTagEnrolled()
        observeTagSearched()

        setOnBackPressed()

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
    }


    private fun setOnBackPressed() {
        onBackPressedListener = object: OnBackPressedInFragment {
            override fun onBackPressed(): Boolean {
                binding.apply {
                    return if (isKeyboardShowing) {
                        KeyboardUtil.keyBoardHide(binding.inputTag)
                        true
                    } else {
                        if(!isChangingFragment) {
                            saveTags()
                            isChangingFragment = true
                            rvTagEnrolled.animateTransparency(0.0f, 2000)
                            layoutInput.animateTransparency(0.0f, 2000)
                            layoutTagRecommend.animateTransparency(0.0f, 2000)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator?) {
                                        super.onAnimationEnd(animation)
                                        sharedViewModel.previousPage.value = CONTROL_TAG
                                        KeyboardUtil.keyBoardHide(binding.inputTag)
                                        parentActivity.onBackPressed(true)
                                    }
                                })
                            true
                        } else {
                            true
                        }
                    }
                }
            }
        }
    }

    private fun initBackground() {
        val brightness: Int = sharedViewModel.brightness.value ?: 0
        val tags: List<Tag> = sharedViewModel.tags.value ?: mutableListOf()

        setEntireTagFragmentColor(brightness)
        gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)

        binding.layoutAddTag.background = gradientDrawable
        tagViewModel.candidateTags.value = tags.toMutableList()
        binding.tvBrightness.text = brightness.toString()
    }

    private fun showUi() {
        binding.apply {
            when (sharedViewModel.previousPage.value) {
                CONTROL_MEMO -> {
                    rvTagEnrolled.alpha = 1.0f
                    layoutInput.animateTransparency(1.0f, 2000)
                    layoutTagRecommend.animateTransparency(1.0f, 2000)
                }
                else -> {
                    rvTagEnrolled.animateTransparency(1.0f, 2000)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator?) {
                                rvTagEnrolled.alpha = 0f
                            }
                        })
                    layoutInput.animateTransparency(1.0f, 2000)
                    layoutTagRecommend.animateTransparency(1.0f, 2000)
                }
            }
        }
    }

    private fun saveTags() {
        sharedViewModel.tags.value = tagViewModel.candidateTags.value
    }

    private fun observeTagEnrolled() {
        tagViewModel.candidateTags.observe(requireActivity()) { enrolled ->
            tagEnrolledAdapter.apply {
                if(isEditMode) {
                    binding.rvTagEnrolled.itemAnimator = null
                    isEditMode = false
                    editModeTag = null
                } else {
                    binding.rvTagEnrolled.itemAnimator = DefaultItemAnimator()
                }

                val brightness = sharedViewModel.brightness.value
                submitList(enrolled.toMutableList(), brightness?:0)
            }
        }
    }

    private fun observeTagSearched() {
        tagViewModel.searchedTagNames.observe(requireActivity()) { searchedResult ->
            val brightness = sharedViewModel.brightness.value

            tagRecommendAdapter.submitList(
                searchedResult.map{ Tag(it) }.toMutableList(),
                brightness?:0,
                true
            )
        }
    }

    private fun goToFragmentEnrollMemo(view: View) {
        KeyboardUtil.keyBoardHide(binding.inputTag)
        binding.layoutInput.animateTransparency(0.0f, 2000)
        binding.layoutTagRecommend.animateTransparency(0.0f, 2000)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    sharedViewModel.previousPage.value = CONTROL_TAG
                    KeyboardUtil.keyBoardHide(binding.inputTag)
                    val navDirection: NavDirections =
                        EnrollTagFragmentDirections.actionEnrollTagFragmentToEnrollMemoFragment()
                    Navigation.findNavController(view).navigate(navDirection)
                }
            })
    }

}
