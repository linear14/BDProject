package com.bd.bdproject.view.fragment.edit

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import com.bd.bdproject.R
import com.bd.bdproject.`interface`.OnTagClickListener
import com.bd.bdproject.`interface`.OnTagDeleteButtonClickListener
import com.bd.bdproject.common.Constant.CONTROL_TAG
import com.bd.bdproject.common.animateTransparency
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.model.Tags
import com.bd.bdproject.databinding.FragmentControlTagBinding
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.view.activity.BitdamEditActivity
import com.bd.bdproject.view.adapter.TagAdapter
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.viewmodel.EditViewModel
import com.bd.bdproject.viewmodel.common.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.common.TagViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

open class EditTagFragment: BaseFragment() {

    private var _binding: FragmentControlTagBinding? = null
    val binding get() = _binding!!

    private val tagViewModel: TagViewModel by viewModel()
    private val lightTagRelationViewModel: LightTagRelationViewModel by viewModel()
    private val editViewModel: EditViewModel by activityViewModels()

    private val parentActivity by lazy {
        activity as BitdamEditActivity
    }

    private val tagEnrolledAdapter by lazy { TagAdapter().also {
        it.onTagClickListener = object: OnTagClickListener {
            override fun onClick(tagName: String) {
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
        it.onTagDeleteButtonClickListener = object: OnTagDeleteButtonClickListener {
            override fun onClick(tagName: String) {
                deleteCandidateTag(tagName)
            }
        }
    } }

    private val tagRecommendAdapter by lazy { TagAdapter().also {
        it.onTagClickListener = object: OnTagClickListener {
            override fun onClick(tagName: String) {
                if(checkIsValidTag(tagName)) {
                    if(tagEnrolledAdapter.isEditMode && tagEnrolledAdapter.editModeTag != null) {
                        editCandidateTag(tagEnrolledAdapter.editModeTag!!, tagName)
                    }
                    else {
                        enrollTagToCandidate(tagName)
                    }
                }
            }

        }
    } }

    var isKeyboardShowing: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlTagBinding.inflate(inflater, container, false)

        // 뷰모델이 메인 쓰레드에서 먼저 생성되어야 하기 때문에 한 번 호출함(지연 초기화로 인해 다른 쓰레드에서 생기니깐 ViewModel 내에서 Observer 추가하는데 문제 생김)
        // https://stackoverflow.com/questions/66923170/android-java-lang-illegalstateexception-method-addobserver-must-be-called-on
        lightTagRelationViewModel
        initView(editViewModel.light?.bright?:0)

        binding.apply {
            inputTag.addTextChangedListener(InputTagWatcher())
            ivTagRecommendInfo.setOnClickListener {
                animateTagRecommendInfo(editViewModel.light?.bright?:0)
            }
            actionEnroll.setOnClickListener {
                KeyboardUtil.keyBoardHide(binding.inputTag)
                updateTag()
            }
            btnBack.setOnClickListener { parentActivity.onBackPressed() }
        }

        observeTagEnrolled()
        observeTagSearched()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        isKeyboardShowing = false
        initData(editViewModel.tags?: mutableListOf())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView(brightness: Int) {
        setEntireTagFragmentColor(brightness)
        parentActivity.updateBackgroundColor(LightUtil.getDiagonalLight(brightness * 2))
        binding.apply {
            tvBrightness.text = brightness.toString()
            actionNext.visibility = View.GONE
            actionEnroll.visibility = View.VISIBLE
            rvTagEnrolled.alpha = 1.0f
            layoutInput.alpha = 1.0f
            layoutTagRecommend.alpha = 1.0f
        }

        setTagRecyclerView()
    }

    private fun initData(tags: List<Tag>) {
        tagViewModel.candidateTags.value = tags.toMutableList()
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

    private fun observeTagEnrolled() {
        tagViewModel.candidateTags.observe(viewLifecycleOwner) { enrolled ->
            val tags = Tags()
            for(i in enrolled?: mutableListOf()) {
                tags.add(i)
            }
            editViewModel.tags = tags

            tagEnrolledAdapter.apply {
                if(isEditMode) {
                    binding.rvTagEnrolled.itemAnimator = null
                    isEditMode = false
                    editModeTag = null
                } else {
                    binding.rvTagEnrolled.itemAnimator = DefaultItemAnimator()
                }
                val brightness = editViewModel.light?.bright ?: 0
                submitList(enrolled.toMutableList(), brightness)
            }
        }
    }

    private fun observeTagSearched() {
        tagViewModel.searchedTagNames.observe(viewLifecycleOwner) { searchedResult ->
            binding.layoutTagRecommend.isVisible = searchedResult.isNotEmpty()

            val brightness = editViewModel.light?.bright ?: 0

            tagRecommendAdapter.submitList(
                searchedResult.map{ Tag(it.name) }.toMutableList(),
                brightness,
                true
            )
        }
    }

    private fun setEntireTagFragmentColor(brightness: Int) {
        binding.apply {
            val textDeleteButtonResource = if(brightness in 0 until 80) R.drawable.ic_delete_for_dark else R.drawable.ic_delete_for_bright
            ivClearText.setImageResource(textDeleteButtonResource)

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

    private fun updateTag() {
        CoroutineScope(Dispatchers.IO).launch {
            val light = editViewModel.light
            val tags = tagViewModel.candidateTags.value?: mutableListOf()

            if(light != null) {
                val updateTagJob = launch { tagViewModel.insertTag(tagViewModel.candidateTags.value) }
                val updateRelationJob = launch { lightTagRelationViewModel.updateRelationsAll(light.dateCode, tags) }

                joinAll(updateTagJob, updateRelationJob)

                launch(Dispatchers.Main) {
                    parentActivity.returnToDetailActivity(CONTROL_TAG)
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
                        enrollTagToCandidate(tagName)
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
