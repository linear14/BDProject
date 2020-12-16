package com.bd.bdproject.ui.light

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.updateMargins
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.bd.bdproject.BitDamApplication.Companion.applicationContext
import com.bd.bdproject.R
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.FragmentAddLightBinding
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.viewmodel.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.LightViewModel
import com.bd.bdproject.viewmodel.TagViewModel
import com.google.android.material.chip.Chip
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

// insert 부분 메서드들이 viewmodel의 메서드를 사용하기는 하는데, 그래도 이걸 viewmodel로 따리 관리를 해야하는건가?

class AddLightFragment: Fragment() {

    private var _binding: FragmentAddLightBinding? = null
    private val binding get() = _binding!!

    private val lightViewModel: LightViewModel by inject()
    private val tagViewModel: TagViewModel by inject()
    private val lightTagRelationViewModel: LightTagRelationViewModel by inject()

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

        observeTagSearched()

        return binding.root
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
        binding.apply {
            val dateCode = insertLight()
            val tagList = insertTag()
            insertRelation(dateCode, tagList)
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

    private fun insertTag(): MutableList<Tag> {
        binding.apply {
            tagViewModel.asyncInsertTag(tagViewModel.candidateTags)
        }
        return tagViewModel.candidateTags
    }

    private fun insertRelation(dateCode: String, tagList: MutableList<Tag>) {
        lightTagRelationViewModel.insertRelation(dateCode, tagList)
    }

    private fun setSeekBarPressListener() {
        binding.apply {
            sbLight.setOnPressListener { step ->
                tvAskCondition.visibility = View.GONE
                tvBrightness.visibility = View.VISIBLE
                flexBoxTagEnrolled.visibility = View.VISIBLE
                tvBrightness.text = getBrightness(step).toString()
                sbLight.barWidth = 4
            }
        }
    }

    private fun setSeekBarProgressChangedListener() {
        binding.apply {
            sbLight.setOnProgressChangeListener { step ->
                tvBrightness.text = getBrightness(step).toString()
            }
        }
    }

    private fun setSeekBarReleaseListener() {
        binding.apply {
            sbLight.setOnReleaseListener { step ->
                sbLight.visibility = View.GONE
                layoutInput.visibility = View.VISIBLE
                layoutTagRecommend.visibility = View.VISIBLE
            }
        }
    }

    private fun getBrightness(step: Int): Int {
        val convertedStep = step / 10
        return (convertedStep * 5)
    }

    private fun makeChip(tagName: String, vg: ViewGroup): Chip {
        val nameWithHash = "# $tagName"
        return Chip(requireActivity()).apply {
            text = nameWithHash
            setTextAppearanceResource(R.style.ChipTextStyle)
        }.also { chip ->
            vg.addView(chip)
            (chip.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(
                left = chip.context.resources.getDimensionPixelSize(R.dimen.chip_margin),
                right = chip.context.resources.getDimensionPixelSize(R.dimen.chip_margin)
            )

            chip.setOnClickListener {
                when(vg) {
                    binding.flexBoxTagEnrolled -> {}
                    binding.flexBoxTagRecommend -> {
                        if(checkIsValidTag(tagName)) {
                            enrollTagToCandidate(tagName)
                        }
                    }
                }
            }
        }
    }

    private fun checkIsValidTag(tagName: String): Boolean {
        val candidateTags = tagViewModel.candidateTags

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
        tagViewModel.candidateTags.add(Tag(tagName))
        makeChip(tagName, binding.flexBoxTagEnrolled)
        binding.inputTag.text.clear()
        binding.flexBoxTagRecommend.removeAllViews()
    }

    private fun observeTagSearched() {
        tagViewModel.searchedTagNames.observe(requireActivity()) {
            binding.flexBoxTagRecommend.removeAllViews()
            for(word in it) {
                makeChip(word, binding.flexBoxTagRecommend)
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
                    withContext(Dispatchers.Main) { binding.flexBoxTagRecommend.removeAllViews() }
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