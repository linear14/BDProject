package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.util.Constant.CONTROL_MEMO
import com.bd.bdproject.util.Constant.CONTROL_TAG
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.util.SharedUtil.isAnimationActive
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.util.screenTransitionAnimationMilliSecond
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.view.fragment.ControlTagFragment
import com.bd.bdproject.viewmodel.EnrollViewModel

open class EnrollTagFragment: ControlTagFragment() {

    private val sharedViewModel: EnrollViewModel by activityViewModels()

    private val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.actionNext.setOnClickListener {
            if(!isChangingFragment) {
                isChangingFragment = true
                saveTags()
                if(isAnimationActive()) {
                    goToFragmentEnrollMemoWithAnimation()
                } else {
                    goToFragmentEnrollMemo()
                }
            }
        }

        binding.ivTagRecommendInfo.setOnClickListener {
            animateTagRecommendInfo(sharedViewModel.brightness.value ?: 0)
        }

        observeTagEnrolled()
        observeTagSearched()

    }

    override fun onResume() {
        super.onResume()

        makeBackground(
            brightness = sharedViewModel.brightness.value ?: 0,
            tags = sharedViewModel.tags.value ?: mutableListOf()
        )
        if(isAnimationActive()) {
            showUiWithAnimation()
        } else {
            showUiWithoutAnimation()
        }

        setOnBackPressed()

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
    }

    private fun showUiWithAnimation() {
        binding.apply {
            when (sharedViewModel.previousPage) {
                CONTROL_MEMO -> {
                    rvTagEnrolled.alpha = 1.0f
                    layoutInput.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                    layoutTagRecommend.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                }
                else -> {
                    rvTagEnrolled.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator?) {
                                rvTagEnrolled.alpha = 0f
                            }
                        })
                    layoutInput.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                    layoutTagRecommend.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                }
            }
        }
    }

    private fun showUiWithoutAnimation() {
        binding.apply {
            actionNext.visibility = View.VISIBLE
            actionEnroll.visibility = View.GONE
            rvTagEnrolled.alpha = 1.0f
            layoutInput.alpha = 1.0f
            layoutTagRecommend.alpha = 1.0f
        }
    }

    private fun observeTagEnrolled() {
        tagViewModel.candidateTags.observe(viewLifecycleOwner) { enrolled ->
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
        tagViewModel.searchedTagNames.observe(viewLifecycleOwner) { searchedResult ->
            val brightness = sharedViewModel.brightness.value

            tagRecommendAdapter.submitList(
                searchedResult.map{ Tag(it.name) }.toMutableList(),
                brightness?:0,
                true
            )
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

                            if(isAnimationActive()) {
                                rvTagEnrolled.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                                layoutInput.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                                layoutTagRecommend.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                                    .setListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator?) {
                                            super.onAnimationEnd(animation)
                                            goBackToFragmentEnrollBrightness()
                                        }
                                    })
                            } else {
                                goBackToFragmentEnrollBrightness()
                            }
                            true
                        } else {
                            true
                        }
                    }
                }
            }
        }
    }

    private fun saveTags() {
        sharedViewModel.tags.value = tagViewModel.candidateTags.value
    }

    private fun goBackToFragmentEnrollBrightness() {
        sharedViewModel.previousPage = CONTROL_TAG
        KeyboardUtil.keyBoardHide(binding.inputTag)
        parentActivity.onBackPressed(true)
    }

    private fun goToFragmentEnrollMemoWithAnimation() {
        KeyboardUtil.keyBoardHide(binding.inputTag)
        binding.layoutInput.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
        binding.layoutTagRecommend.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    goToFragmentEnrollMemo()
                }
            })
    }

    private fun goToFragmentEnrollMemo() {
        sharedViewModel.previousPage = CONTROL_TAG
        KeyboardUtil.keyBoardHide(binding.inputTag)
        val navDirection: NavDirections =
            EnrollTagFragmentDirections.actionEnrollTagFragmentToEnrollMemoFragment()
        Navigation.findNavController(binding.root).navigate(navDirection)
    }

}
