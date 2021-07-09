package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import com.bd.bdproject.R
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.`interface`.OnTagClickListener
import com.bd.bdproject.`interface`.OnTagDeleteButtonClickListener
import com.bd.bdproject.common.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.common.Constant.CONTROL_MEMO
import com.bd.bdproject.common.Constant.CONTROL_TAG
import com.bd.bdproject.common.animateTransparency
import com.bd.bdproject.common.screenTransitionAnimationMilliSecond
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.FragmentControlTagBinding
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.SharedUtil.isAnimationActive
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.view.adapter.TagAdapter
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.viewmodel.EnrollViewModel
import com.bd.bdproject.viewmodel.common.TagViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import gun0912.tedkeyboardobserver.TedKeyboardObserver
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

open class EnrollTagFragment: BaseFragment() {

    private var _binding: FragmentControlTagBinding? = null
    val binding get() = _binding!!

    private val sharedViewModel: EnrollViewModel by activityViewModels()
    private val tagViewModel: TagViewModel by viewModel()

    private val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

    val tagEnrolledAdapter by lazy { TagAdapter().also {
        it.onTagClickListener = object: OnTagClickListener {
            override fun onClick(tagName: String) {
                if(!sharedViewModel.isFragmentTransitionState) {
                    if(it.isEditMode) {
                        binding.inputTag.setText(null)
                        it.isEditMode = false
                        it.editModeTag = null
                        tagViewModel.searchTag(null)
                    } else {
                        binding.inputTag.setText(tagName)
                        binding.inputTag.setSelection(binding.inputTag.text.length)
                        it.isEditMode = true
                        it.editModeTag = tagName
                    }
                    it.notifyDataSetChanged()
                }
            }
        }
        it.onTagDeleteButtonClickListener = object: OnTagDeleteButtonClickListener {
            override fun onClick(tagName: String) {
                if(!sharedViewModel.isFragmentTransitionState) {
                    deleteCandidateTag(tagName)
                }
            }
        }
    } }

    val tagRecommendAdapter by lazy { TagAdapter().also {
        it.onTagClickListener = object: OnTagClickListener {
            override fun onClick(tagName: String) {
                if(checkIsValidTag(tagName)) {
                    if(tagEnrolledAdapter.isEditMode && tagEnrolledAdapter.editModeTag != null) {
                        editCandidateTag(tagEnrolledAdapter.editModeTag!!, tagName)
                    }
                    else {
                        if(!sharedViewModel.isFragmentTransitionState) { enrollTagToCandidate(tagName) }
                    }
                }
            }

        }
    } }

    var isKeyboardShowing: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentControlTagBinding.inflate(inflater, container, false)

        initDataAndBackground()

        when {
            // 이전 페이지에서 넘어왔을 경우
            sharedViewModel.previousPage == CONTROL_BRIGHTNESS -> {
                if(isAnimationActive()) {
                    showUiWithAnimationFromPreviousPage()
                } else {
                    showUiWithoutAnimation()
                }
            }

            // 다음 페이지에서 돌아왔을 경우
            sharedViewModel.previousPage == CONTROL_MEMO -> {
                if(isAnimationActive()) {
                    showUiWithAnimationFromNextPage()
                } else {
                    showUiWithoutAnimation()
                }
            }

            // 기타 상황
            else -> {
                showUiWithoutAnimation()
            }
        }

        binding.apply {
            inputTag.addTextChangedListener(InputTagWatcher())

            ivClearText.setOnClickListener {
                inputTag.setText(null)
                tagViewModel.searchTag(null)
            }

            ivTagRecommendInfo.setOnClickListener {
                animateTagRecommendInfo(sharedViewModel.brightness)
            }

            actionNext.setOnClickListener {
                if(!sharedViewModel.isFragmentTransitionState) {
                    saveTags()

                    if(isAnimationActive()) {
                        goToFragmentEnrollMemoWithAnimation()
                    } else {
                        goToFragmentEnrollMemo()
                    }
                }
            }

            btnBack.setOnClickListener {
                KeyboardUtil.keyBoardHide(binding.inputTag)
                isKeyboardShowing = false
                parentActivity.onBackPressed()
            }
        }

        setOnBackPressed()

        observeTagEnrolled()
        observeTagSearched()
        observeKeyboard()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        isKeyboardShowing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // region View

    private fun initDataAndBackground() {
        binding.apply {
            actionEnroll.visibility = View.GONE

            sharedViewModel.brightness.also {
                parentActivity.updateBackgroundColor(LightUtil.getDiagonalLight(it * 2))
                tvBrightness.text = it.toString()
                setEntireTagFragmentColor(it)
            }

            setTagRecyclerView()
            tagViewModel.candidateTags.value = sharedViewModel.tags.toMutableList()
        }
    }

    private fun setTagRecyclerView() {
        binding.apply {
            val layoutManagerEnrolled = FlexboxLayoutManager(requireActivity()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER

            }

            val layoutManagerRecommend = FlexboxLayoutManager(requireActivity()).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.CENTER
            }

            rvTagEnrolled.layoutManager = layoutManagerEnrolled
            rvTagRecommend.layoutManager = layoutManagerRecommend

            rvTagEnrolled.adapter = tagEnrolledAdapter
            rvTagRecommend.adapter = tagRecommendAdapter
        }
    }

    private fun setEntireTagFragmentColor(brightness: Int) {
        binding.apply {
            val textDeleteButtonResource = if(brightness in 0 until 80) R.drawable.ic_delete_for_dark else R.drawable.ic_delete_for_bright
            ivClearText.setImageResource(textDeleteButtonResource)

            ColorUtil.setEntireViewColor(
                brightness,
                tvBrightness,
                actionNext,
                actionEnroll,
                tvHash,
                inputTag,
                separator1,
                tvTagRecommend,
                ivTagRecommendInfo,
                btnBack
            )
        }
    }

    private fun animateTagRecommendInfo(brightness: Int) {
        binding.apply {
            tvTagRecommendInfo.animateTransparency(1.0f, 500)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        ivTagRecommendInfo.setImageResource(R.drawable.ic_info_fill)
                        tvTagRecommendInfo.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)

                        CoroutineScope(Dispatchers.IO).launch {
                            delay(2000)

                            launch(Dispatchers.Main) {
                                tvTagRecommendInfo.animateTransparency(0.0f, 500)
                                    .setListener(object: AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator?) {
                                            super.onAnimationEnd(animation)
                                            ivTagRecommendInfo.setImageResource(R.drawable.ic_info_outline)
                                            ColorUtil.setEntireViewColor(brightness, ivTagRecommendInfo)
                                            tvTagRecommendInfo.visibility = View.GONE
                                        }
                                    })
                            }
                        }
                    }
                })
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

                val brightness = sharedViewModel.brightness
                submitList(enrolled.toMutableList(), brightness)
            }
        }
    }

    private fun observeTagSearched() {
        tagViewModel.searchedTagNames.observe(viewLifecycleOwner) { searchedResult ->
            val brightness = sharedViewModel.brightness

            tagRecommendAdapter.submitList(
                searchedResult.map{ Tag(it.name) }.toMutableList(),
                brightness,
                true
            )
        }
    }

    private fun observeKeyboard() {
        TedKeyboardObserver(requireActivity()).listen { isShow ->
            isKeyboardShowing = isShow
        }
    }

    // endregion


    // region Transition

    private fun showUiWithAnimationFromPreviousPage() {
        sharedViewModel.isFragmentTransitionState = true
        binding.apply {
            rvTagEnrolled.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                .setListener(object : AnimatorListenerAdapter() {})
            layoutInput.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                .setListener(object : AnimatorListenerAdapter() {})
            layoutTagRecommend.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        sharedViewModel.isFragmentTransitionState = false
                    }
                })
        }
    }

    private fun showUiWithAnimationFromNextPage() {
        sharedViewModel.isFragmentTransitionState = true
        binding.apply {
            rvTagEnrolled.alpha = 1.0f
            layoutInput.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                .setListener(object : AnimatorListenerAdapter() {})
            layoutTagRecommend.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        sharedViewModel.isFragmentTransitionState = false
                    }
                })
        }
    }

    private fun showUiWithoutAnimation() {
        sharedViewModel.isFragmentTransitionState = true
        binding.apply {
            rvTagEnrolled.alpha = 1.0f
            layoutInput.alpha = 1.0f
            layoutTagRecommend.alpha = 1.0f
        }
        sharedViewModel.isFragmentTransitionState = false
    }

    private fun goToFragmentEnrollMemoWithAnimation() {
        sharedViewModel.isFragmentTransitionState = true
        KeyboardUtil.keyBoardHide(binding.inputTag)
        binding.apply {
            layoutInput.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object : AnimatorListenerAdapter() {})
            layoutTagRecommend.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        sharedViewModel.isFragmentTransitionState = false
                        goToFragmentEnrollMemo()
                    }
                })
        }
    }

    private fun goToFragmentEnrollMemo() {
        sharedViewModel.isFragmentTransitionState = true
        KeyboardUtil.keyBoardHide(binding.inputTag)
        saveTags()
        sharedViewModel.previousPage = CONTROL_TAG
        val navDirection: NavDirections =
            EnrollTagFragmentDirections.actionEnrollTagFragmentToEnrollMemoFragment()
        Navigation.findNavController(parentActivity.binding.layoutFragment).navigate(navDirection)
        sharedViewModel.isFragmentTransitionState = false

    }

    private fun goBackToFragmentEnrollBrightnessWithAnimation() {
        sharedViewModel.isFragmentTransitionState = true
        KeyboardUtil.keyBoardHide(binding.inputTag)
        binding.apply {
            rvTagEnrolled.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter() {})
            layoutInput.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object : AnimatorListenerAdapter() {})
            layoutTagRecommend.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        sharedViewModel.isFragmentTransitionState = false
                        goBackToFragmentEnrollBrightness()
                    }
                })
        }
    }

    private fun goBackToFragmentEnrollBrightness() {
        sharedViewModel.isFragmentTransitionState = true
        sharedViewModel.previousPage = CONTROL_TAG
        KeyboardUtil.keyBoardHide(binding.inputTag)
        (activity as BitdamEnrollActivity).onBackPressed(true)
        sharedViewModel.isFragmentTransitionState = false
    }

    // endregion


    // region Logic

    private fun checkIsValidTag(tagName: String): Boolean {
        val candidateTags = tagViewModel.candidateTags.value?: mutableListOf()

        // 태그 갯수가 4개 이상 (등록)
        if (!tagEnrolledAdapter.isEditMode && candidateTags.size >= 4) {
            Toast.makeText(requireContext(), "태그는 최대 4개까지 등록 가능합니다.", Toast.LENGTH_SHORT).show()
            binding.inputTag.setText(tagName)
            binding.inputTag.setSelection(binding.inputTag.text.length)
            return false
        }

        // 태그명 중복 (등록, 수정)
        if ((!tagEnrolledAdapter.isEditMode && tagName in candidateTags.map { it.name }) ||
            (tagEnrolledAdapter.isEditMode && tagName in candidateTags
                .filter { tag -> tag.name != tagEnrolledAdapter.editModeTag }
                .map { it.name })) {
            Toast.makeText(requireContext(), "태그명이 중복되었습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
            binding.inputTag.setText(tagName)
            binding.inputTag.setSelection(binding.inputTag.text.length)
            return false
        }

        return true
    }


    private fun enrollTagToCandidate(tagName: String) {
        tagViewModel.insertTagToCandidate(tagName)
        binding.inputTag.text.clear()
        tagViewModel.searchTag(null)
    }

    private fun editCandidateTag(oldTagName: String, newTagName: String) {
        tagViewModel.editTagCandidate(oldTagName, newTagName)
        binding.inputTag.text.clear()
        tagViewModel.searchTag(null)
    }

    private fun deleteCandidateTag(tagName: String) {
        tagViewModel.deleteTagCandidate(tagName)
        binding.inputTag.text.clear()
        tagViewModel.searchTag(null)
    }


    private fun saveTags() {
        sharedViewModel.tags = tagViewModel.candidateTags.value?: mutableListOf()
    }

    private fun setOnBackPressed() {
        onBackPressedListener = object: OnBackPressedInFragment {
            override fun onBackPressed(): Boolean {
                binding.apply {
                    return if (isKeyboardShowing) {
                        KeyboardUtil.keyBoardHide(binding.inputTag)
                        true
                    } else {
                        if(!sharedViewModel.isFragmentTransitionState) {
                            saveTags()

                            if(isAnimationActive()) {
                                goBackToFragmentEnrollBrightnessWithAnimation()
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


    /***
     *  EditText 변화 감지 TextWatcher
     *  1. 태그 등록 감지 (띄어쓰기 관련 처리)
     *  2. 검색 기능
     */
    inner class InputTagWatcher: TextWatcher {

        var searchJob: Job? = null

        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if(s.isBlank() || s.isEmpty()) {
                binding.ivClearText.visibility = View.INVISIBLE
                binding.inputTag.text.clear()

                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(500)
                    tagViewModel.searchTag(null)
                }
                return
            }

            binding.ivClearText.visibility = View.VISIBLE

            if(checkIsThereAnyBlank(s)) {
                Toast.makeText(requireContext(), "공백이 포함된 태그는 등록할 수 없습니다.", Toast.LENGTH_SHORT).show()

                val selectionStart = binding.inputTag.selectionStart
                val split = s.split(" ")
                binding.inputTag.setText(split[0] + split[1])
                binding.inputTag.setSelection(selectionStart - 1)
                return
            }

            val lastIndex = s.length - 1
            val tagName = s.substring(0, lastIndex)

            // 길이가 9일때 마지막 단어가 공백이 아니면 8자리로 돌리고 return
            if(s.length == 9 && s[lastIndex] != ' ') {
                Toast.makeText(requireContext(), "최대 8글자까지 가능합니다.", Toast.LENGTH_SHORT).show()

                val selectionStart = binding.inputTag.selectionStart
                binding.inputTag.setText(binding.inputTag.text.dropLast(1))
                binding.inputTag.setSelection(selectionStart - 1)
                return
            }

            // 추천 검색어
            if(!isLastWordBlank(s)) {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(500)
                    if(s.isNotBlank()) {
                        tagViewModel.searchTag(s.toString())
                    }
                }
            }

            if(isLastWordBlank(s)) {
                if(checkIsValidTag(tagName)) {
                    if(tagEnrolledAdapter.isEditMode && tagEnrolledAdapter.editModeTag != null) {
                        editCandidateTag(tagEnrolledAdapter.editModeTag!!, tagName)
                    }
                    else {
                        if(!sharedViewModel.isFragmentTransitionState) { enrollTagToCandidate(tagName) }
                    }
                }
            }
        }

        // 공백이 중간에 끼어있을 경우를 검사 (마지막 스페이스는 제외)
        private fun checkIsThereAnyBlank(s: CharSequence): Boolean =
            (s.isBlank()) || ((!isLastWordBlank(s)) && s.contains(" "))

        private fun isLastWordBlank(s: CharSequence): Boolean =
            s[s.length - 1].toInt() == ' '.toInt()

    }

}
