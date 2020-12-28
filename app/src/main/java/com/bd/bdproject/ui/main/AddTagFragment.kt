package com.bd.bdproject.ui.main

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DefaultItemAnimator
import com.bd.bdproject.BitDamApplication
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.`interface`.OnTagClickListener
import com.bd.bdproject.`interface`.OnTagDeleteButtonClickListener
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.FragmentAddTagBinding
import com.bd.bdproject.ui.BaseFragment
import com.bd.bdproject.ui.MainActivity
import com.bd.bdproject.ui.MainActivity.Companion.ADD_MEMO
import com.bd.bdproject.ui.main.adapter.TagAdapter
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.viewmodel.TagViewModel
import com.bd.bdproject.viewmodel.main.AddViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import gun0912.tedkeyboardobserver.TedKeyboardObserver
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class AddTagFragment: BaseFragment() {

    private var _binding: FragmentAddTagBinding? = null
    private val binding get() = _binding!!

    private val tagViewModel: TagViewModel by inject()
    private val sharedViewModel: AddViewModel by activityViewModels()

    private var tagEnrolledAdapter = TagAdapter()
    private var tagRecommendAdapter = TagAdapter()

    private val gradientDrawable = GradientDrawable().apply {
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
        _binding = FragmentAddTagBinding.inflate(inflater, container, false).apply {
            inputTag.addTextChangedListener(InputTagWatcher())
            actionNext.setOnClickListener {
                if(!isChangingFragment) {
                    isChangingFragment = true
                    saveTags()
                    goToFragmentAddMemo(it)
                }
            }
        }
        initBackground()
        showUi()
        setTagRecyclerView()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        isKeyboardShowing = false
        isChangingFragment = false

        observeTagEnrolled()
        observeTagSearched()
        observeKeyboard()
        setOnBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                                        sharedViewModel.previousPage.value = MainActivity.ADD_TAG
                                        KeyboardUtil.keyBoardHide(binding.inputTag)
                                        mainActivity.onBackPressed(true)
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

    private fun checkIsValidTag(tagName: String): Boolean {
        val candidateTags = tagViewModel.candidateTags.value?: mutableListOf()

        // 태그 갯수가 4개 이상 (등록)
        if (!tagEnrolledAdapter.isEditMode && candidateTags.size >= 4) {
            Toast.makeText(BitDamApplication.applicationContext(), "태그는 최대 4개까지 등록 가능합니다.", Toast.LENGTH_SHORT).show()
            binding.inputTag.setText(tagName)
            binding.inputTag.setSelection(binding.inputTag.text.length)
            return false
        }

        // 태그명 중복 (등록, 수정)
        if ((!tagEnrolledAdapter.isEditMode && tagName in candidateTags.map { it.name }) ||
            (tagEnrolledAdapter.isEditMode && tagName in candidateTags
                .filter { tag -> tag.name != tagEnrolledAdapter.editModeTag }
                .map { it.name })) {
            Toast.makeText(BitDamApplication.applicationContext(), "태그명이 중복되었습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT)
                .show()
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
                submitList(enrolled.toMutableList(), sharedViewModel.brightness.value?:0)
            }
        }
    }

    private fun observeTagSearched() {
        tagViewModel.searchedTagNames.observe(requireActivity()) { searchedResult ->
            tagRecommendAdapter.submitList(
                searchedResult.map{ Tag(it) }.toMutableList(),
                sharedViewModel.brightness.value?:0,
                true
            )
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

            rvTagEnrolled.adapter = tagEnrolledAdapter.also {
                it.onTagClickListener = object: OnTagClickListener {
                    override fun onClick(tagName: String) {
                        if(!isChangingFragment) {
                            inputTag.setText(tagName)
                            inputTag.setSelection(inputTag.text.length)
                            it.isEditMode = true
                            it.editModeTag = tagName
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
            }

            rvTagRecommend.adapter = tagRecommendAdapter.also {
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
            }
        }
    }

    private fun initBackground() {
        mainActivity.binding.btnDrawer.visibility = View.GONE
        mainActivity.binding.btnBack.visibility = View.VISIBLE

        val brightness = sharedViewModel.brightness.value?:0
        val tags = sharedViewModel.tags.value?.toMutableList()?: mutableListOf()

        setEntireTagFragmentColor(brightness)
        tagViewModel.candidateTags.value = tags
        gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
        binding.layoutAddTag.background = gradientDrawable
        binding.tvBrightness.text = brightness.toString()
    }

    private fun showUi() {
        if(sharedViewModel.previousPage.value == ADD_MEMO) {
            binding.rvTagEnrolled.alpha = 1.0f
        } else {
            binding.rvTagEnrolled.animateTransparency(1.0f, 2000)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        binding.rvTagEnrolled.alpha = 0f
                    }
                })
        }

        binding.layoutInput.animateTransparency(1.0f, 2000)
        binding.layoutTagRecommend.animateTransparency(1.0f, 2000)
    }

    private fun saveTags() {
        sharedViewModel.tags.value = tagViewModel.candidateTags.value
    }

    private fun goToFragmentAddMemo(view: View) {
        KeyboardUtil.keyBoardHide(binding.inputTag)
        binding.layoutInput.animateTransparency(0.0f, 2000)
        binding.layoutTagRecommend.animateTransparency(0.0f, 2000)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    sharedViewModel.previousPage.value = MainActivity.ADD_TAG
                    KeyboardUtil.keyBoardHide(binding.inputTag)
                    val navDirection: NavDirections = AddTagFragmentDirections.actionAddTagFragmentToAddMemoFragment()
                    Navigation.findNavController(view).navigate(navDirection)
                }
            })
    }

    private fun observeKeyboard() {
        TedKeyboardObserver(requireActivity()).listen { isShow ->
            isKeyboardShowing = isShow
        }
    }

    private fun setEntireTagFragmentColor(brightness: Int) {
        binding.apply {
            ColorUtil.setEntireViewColor(
                brightness,
                tvBrightness,
                actionNext,
                tvHash,
                inputTag,
                separator1,
                tvTagRecommend,
                mainActivity.binding.btnBack
            )
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
                searchJob?.cancel()
                searchJob = GlobalScope.launch {
                    delay(500)
                    tagViewModel.searchTag(null)
                }
            }
            if(s.isNotEmpty()) {
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
                Toast.makeText(BitDamApplication.applicationContext(), "공백이 포함된 태그는 등록할 수 없습니다.", Toast.LENGTH_SHORT).show()
                binding.inputTag.text.clear()
                return true
            }
            return false
        }

        private fun isLastWordBlank(s: CharSequence): Boolean =
            s[s.length - 1].toInt() == ' '.toInt()

    }

}