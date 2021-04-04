package com.bd.bdproject.view.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bd.bdproject.`interface`.OnTagClickListener
import com.bd.bdproject.`interface`.OnTagDeleteButtonClickListener
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.FragmentControlTagBinding
import com.bd.bdproject.util.BitDamApplication
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.view.adapter.TagAdapter
import com.bd.bdproject.viewmodel.common.TagViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.snackbar.Snackbar
import gun0912.tedkeyboardobserver.TedKeyboardObserver
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

open class ControlTagFragment: BaseFragment() {

    private var _binding: FragmentControlTagBinding? = null
    val binding get() = _binding!!

    val tagViewModel: TagViewModel by inject()

    val tagEnrolledAdapter by lazy { TagAdapter().also {
        it.onTagClickListener = object: OnTagClickListener {
            override fun onClick(tagName: String) {
                if(!isChangingFragment) {
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
                if(!isChangingFragment) {
                    deleteTag(tagName)
                }
            }
        }
    } }

    val tagRecommendAdapter by lazy { TagAdapter().also {
        it.onTagClickListener = object: OnTagClickListener {
            override fun onClick(tagName: String) {
                if(checkIsValidTag(tagName)) {
                    if(tagEnrolledAdapter.isEditMode && tagEnrolledAdapter.editModeTag != null) {
                        editTag(tagEnrolledAdapter.editModeTag!!, tagName)
                    }
                    else {
                        if(!isChangingFragment) { enrollTagToCandidate(tagName) }
                    }
                }
            }

        }
    } }

    val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    /***
     *  @flag
     *  - isKeyboardShowing:
     *  소프트 키보드가 보이는지 감춰져있는지 판단하는 플래그.
     *  onBackPressed를 활용하기위해 박상권님의 라이브러리를 사용했다.
     *
     *  - isChangingFragment :
     *      다음 화면으로 전환 애니메이션이 동작하면 true로 변합니다.
     *      true 상태에서는 추가적인 값의 조작이 불가능합니다.
     */
    var isKeyboardShowing: Boolean = false
    var isChangingFragment = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentControlTagBinding.inflate(inflater, container, false).apply {
            inputTag.addTextChangedListener(InputTagWatcher())

            ivTagRecommendInfo.setOnClickListener {
                tvTagRecommendInfo.animateTransparency(1.0f, 500)
                    .setListener(object: AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            super.onAnimationStart(animation)
                            tvTagRecommendInfo.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)

                            GlobalScope.launch {
                                delay(2000)

                                GlobalScope.launch(Dispatchers.Main) {
                                    tvTagRecommendInfo.animateTransparency(0.0f, 500)
                                        .setListener(object: AnimatorListenerAdapter() {
                                            override fun onAnimationEnd(animation: Animator?) {
                                                super.onAnimationEnd(animation)
                                                tvTagRecommendInfo.visibility = View.GONE
                                            }
                                        })
                                }
                            }
                        }
                    })

            }

            ivClearText.setOnClickListener {
                inputTag.setText(null)
                tagViewModel.searchTag(null)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTagRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        isKeyboardShowing = false
        isChangingFragment = false

        observeKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun makeBackground(brightness: Int, tags: List<Tag>) {
        setEntireTagFragmentColor(brightness)
        gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)

        binding.layoutAddTag.background = gradientDrawable
        tagViewModel.candidateTags.value = tags.toMutableList()
        binding.tvBrightness.text = brightness.toString()
    }


    private fun checkIsValidTag(tagName: String): Boolean {
        val candidateTags = tagViewModel.candidateTags.value?: mutableListOf()

        // 태그 갯수가 4개 이상 (등록)
        if (!tagEnrolledAdapter.isEditMode && candidateTags.size >= 4) {
            Snackbar.make(binding.root, "태그는 최대 4개까지 등록 가능합니다.", Snackbar.LENGTH_SHORT).show()
            binding.inputTag.setText(tagName)
            binding.inputTag.setSelection(binding.inputTag.text.length)
            return false
        }

        // 태그명 중복 (등록, 수정)
        if ((!tagEnrolledAdapter.isEditMode && tagName in candidateTags.map { it.name }) ||
            (tagEnrolledAdapter.isEditMode && tagName in candidateTags
                .filter { tag -> tag.name != tagEnrolledAdapter.editModeTag }
                .map { it.name })) {
            Snackbar.make(binding.root, "태그명이 중복되었습니다. 다시 입력해주세요.", Snackbar.LENGTH_SHORT).show()
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

    private fun editTag(oldTagName: String, newTagName: String) {
        tagViewModel.editTagCandidate(oldTagName, newTagName)
        binding.inputTag.text.clear()
        tagViewModel.searchTag(null)
    }

    private fun deleteTag(tagName: String) {
        tagViewModel.deleteTagCandidate(tagName)
        binding.inputTag.text.clear()
        tagViewModel.searchTag(null)
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

    fun setEntireTagFragmentColor(brightness: Int) {
        binding.apply {
            ColorUtil.setEntireViewColor(
                brightness,
                tvBrightness,
                ivClearText,
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

    private fun observeKeyboard() {
        TedKeyboardObserver(requireActivity()).listen { isShow ->
            isKeyboardShowing = isShow
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
            if(s.isEmpty()) {
                binding.ivClearText.visibility = View.INVISIBLE

                searchJob?.cancel()
                searchJob = GlobalScope.launch {
                    delay(500)
                    tagViewModel.searchTag(null)
                }
            }
            if(s.isNotEmpty()) {
                binding.ivClearText.visibility = View.VISIBLE

                // 추천 검색어
                if(!isLastWordBlank(s)) {
                    searchJob?.cancel()
                    searchJob = GlobalScope.launch {
                        delay(500)
                        if(s.isNotBlank()) {
                            tagViewModel.searchTag(s.toString())
                        }
                    }
                }

                val lastIndex = s.length - 1
                val tagName = s.substring(0, lastIndex)

                if(checkIsThereAnyBlank(s)) { return }

                if(isLastWordBlank(s)) {
                    if(checkIsValidTag(tagName)) {
                        if(tagEnrolledAdapter.isEditMode && tagEnrolledAdapter.editModeTag != null) {
                            editTag(tagEnrolledAdapter.editModeTag!!, tagName)
                        }
                        else {
                            if(!isChangingFragment) { enrollTagToCandidate(tagName) }
                        }
                    }
                }
            }
        }

        // 공백이 중간에 끼어있을 경우를 검사 (마지막 스페이스는 제외)
        private fun checkIsThereAnyBlank(s: CharSequence): Boolean {
            if((s.isBlank()) || ((!isLastWordBlank(s)) && s.contains(" ")))  {
                Snackbar.make(binding.root, "공백이 포함된 태그는 등록할 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                binding.inputTag.text.clear()
                return true
            }
            return false
        }

        private fun isLastWordBlank(s: CharSequence): Boolean =
            s[s.length - 1].toInt() == ' '.toInt()

    }

}
