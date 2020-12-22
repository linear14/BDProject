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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.bd.bdproject.BitDamApplication.Companion.applicationContext
import com.bd.bdproject.R
import com.bd.bdproject.`interface`.JobFinishedListener
import com.bd.bdproject.`interface`.OnTagClickListener
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.FragmentAddLightBinding
import com.bd.bdproject.ui.main.adapter.TagAdapter
import com.bd.bdproject.util.LightUtil.getDiagonalLight
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.viewmodel.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.LightViewModel
import com.bd.bdproject.viewmodel.TagViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

// insert 부분 메서드들이 viewmodel의 메서드를 사용하기는 하는데, 그래도 이걸 viewmodel로 따리 관리를 해야하는건가?

class AddLightFragment: Fragment() {

    private var _binding: FragmentAddLightBinding? = null

    private val binding get() = _binding!!

    private val lightViewModel: LightViewModel by inject()
    private val tagViewModel: TagViewModel by inject()
    private val lightTagRelationViewModel: LightTagRelationViewModel by inject()

    private var tagEnrolledAdapter = TagAdapter()
    private var tagRecommendAdapter = TagAdapter()

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    private var jobFinishedListener: JobFinishedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAddLightBinding.inflate(inflater, container, false).apply {
            sbLight.thumbPlaceholderDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.deco_seekbar_thumb)
            inputTag.addTextChangedListener(InputTagWatcher())
            btnEnroll.setOnClickListener { insertLightWithTag() }
        }
        showUiWithDelay()

        setSeekBarPressListener()
        setSeekBarProgressChangedListener()
        setSeekBarReleaseListener()

        observeTagEnrolled()
        observeTagSearched()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTagRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showUiWithDelay() {
        GlobalScope.launch {
            binding.apply {
                tvAskCondition.visibility = View.GONE
                tvAskCondition.clearAnimation()
                sbLight.clearAnimation()

                delay(1000)

                withContext(Dispatchers.Main) {
                    tvAskCondition.animateTransparency(1.0f, 2000)
                        .setListener(object: AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator?) {
                                super.onAnimationStart(animation)
                                tvAskCondition.visibility = View.VISIBLE
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                sbLight.animateTransparency(1.0f, 2000)
                            }
                        })
                }
            }
        }
    }

    private fun insertLightWithTag() {
        runBlocking {
            binding.apply {
                val dateCode = GlobalScope.async { insertLight() }
                val tagList = GlobalScope.async { insertTag() }
                val job = GlobalScope.launch { insertRelation(dateCode.await(), tagList.await()) }

                job.join()

                // 왜 withContext로는 안되지? --> 완료된 함수 안에서만 사용?
                if(job.isCancelled) {
                    // TODO 예외 처리를 어떻게 할까? __ 이렇게 예외 처리하는건 맞는지 알아보기
                    Toast.makeText(applicationContext(), "등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(applicationContext(), "빛 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        jobFinishedListener?.onSuccess()
                    }
                }

            }
        }

    }

    private fun insertLight(): String {
        binding.apply {
            val currentTime = System.currentTimeMillis().timeToString()
            val light = Light(
                currentTime,
                tvBrightness.text.toString().toInt(),
                null
            )
            lightViewModel.asyncInsertLight(light)
            return currentTime
        }
    }

    private fun insertTag(): MutableList<Tag>? {
        binding.apply {
            tagViewModel.asyncInsertTag(tagViewModel.candidateTags.value)
        }
        return tagViewModel.candidateTags.value
    }

    private fun insertRelation(dateCode: String, tagList: MutableList<Tag>?) {
        lightTagRelationViewModel.insertRelation(dateCode, tagList)
    }

    private fun setSeekBarPressListener() {
        binding.apply {
            sbLight.setOnPressListener { progress ->
                tvAskCondition.visibility = View.GONE
                tvBrightness.visibility = View.VISIBLE
                rvTagEnrolled.visibility = View.VISIBLE
                tvBrightness.text = getBrightness(progress).toString()
                sbLight.barWidth = 4
            }
        }
    }

    private fun setSeekBarProgressChangedListener() {
        binding.apply {
            sbLight.setOnProgressChangeListener { progress ->
                tvBrightness.text = getBrightness(progress).toString()

                gradientDrawable.colors = getDiagonalLight(progress)
                layoutAddLight.background = gradientDrawable
            }
        }
    }

    private fun setSeekBarReleaseListener() {
        binding.apply {
            sbLight.setOnReleaseListener { progress ->
                sbLight.visibility = View.GONE
                layoutInput.visibility = View.VISIBLE
                layoutTagRecommend.visibility = View.VISIBLE
            }
        }
    }

    private fun getBrightness(progress: Int): Int {
        val converted = progress / 10
        return (converted * 5)
    }

    private fun checkIsValidTag(tagName: String): Boolean {
        val candidateTags = tagViewModel.candidateTags.value?: mutableListOf()

        // 태그 갯수가 4개 이상
        if (candidateTags.size >= 4) {
            Toast.makeText(applicationContext(), "태그는 최대 4개까지 등록 가능합니다.", Toast.LENGTH_SHORT).show()
            binding.inputTag.setText(tagName)
            binding.inputTag.setSelection(binding.inputTag.text.length)
            return false
        }

        // 태그명 중복
        if (tagName in candidateTags.map { it.name }) {
            Toast.makeText(applicationContext(), "태그명이 중복되었습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT)
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

    private fun observeTagEnrolled() {
        tagViewModel.candidateTags.observe(requireActivity()) { enrolled ->
            tagEnrolledAdapter.submitList(enrolled.toMutableList())
        }
    }

    private fun observeTagSearched() {
        tagViewModel.searchedTagNames.observe(requireActivity()) { searchedResult ->
            tagRecommendAdapter.submitList(searchedResult.map{ Tag(it) } )
        }
    }

    fun setOnJobFinishedListener(li: JobFinishedListener) {
        this.jobFinishedListener = li
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
            rvTagRecommend.adapter = tagRecommendAdapter.also {
                it.onTagClickListener = object: OnTagClickListener {
                    override fun onClick(tagName: String) {
                        if(checkIsValidTag(tagName)) {
                            enrollTagToCandidate(tagName)
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
                        enrollTagToCandidate(tagName)
                    }
                }
            }
        }

        // 공백이 중간에 끼어있을 경우를 검사 (마지막 스페이스는 제외)
        private fun checkIsThereAnyBlank(s: CharSequence): Boolean {
            if((s.isBlank()) || ((!isLastWordBlank(s)) && s.contains(" ")))  {
                Toast.makeText(applicationContext(), "공백이 포함된 태그는 등록할 수 없습니다.", Toast.LENGTH_SHORT).show()
                binding.inputTag.text.clear()
                return true
            }
            return false
        }

        private fun isLastWordBlank(s: CharSequence): Boolean =
            s[s.length - 1].toInt() == ' '.toInt()

    }
}