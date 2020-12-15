package com.bd.bdproject.project.light

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

    private fun makeChip(name: String, vg: ViewGroup): Chip {
        val nameWithHash = "# $name"
        return Chip(requireActivity()).apply {
            text = nameWithHash
            setTextAppearanceResource(R.style.ChipTextStyle)
        }.also {
            vg.addView(it)
            (it.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(
                left = it.context.resources.getDimensionPixelSize(R.dimen.chip_margin),
                right = it.context.resources.getDimensionPixelSize(R.dimen.chip_margin)
            )
        }
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

        var job: Job? = null

        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if(s.isEmpty()) {
                job?.cancel()
                job = GlobalScope.launch {
                    delay(500)
                    withContext(Dispatchers.Main) { binding.flexBoxTagRecommend.removeAllViews() }
                }
            }
            if(s.isNotEmpty()) {

                // 추천 검색어
                if(!isLastWordBlank(s)) {
                    job?.cancel()
                    job = GlobalScope.launch {
                        delay(500)
                        if(s.isNotBlank()) {
                            tagViewModel.searchTag(s.toString())
                        }
                    }
                }

                val lastIndex = s.length - 1
                val tagName = s.substring(0, lastIndex)

                // 공백이 중간에 끼어있을 경우
                if((s.isBlank()) || ((!isLastWordBlank(s)) && s.contains(" ")))  {
                    Toast.makeText(applicationContext(), "공백이 포함된 태그는 등록할 수 없습니다.", Toast.LENGTH_SHORT).show()
                    binding.inputTag.text.clear()
                    return
                }

                if(isLastWordBlank(s)) {
                    when {
                        // 태그가 4개 이미 등록되어 있을 경우
                        tagViewModel.candidateTags.size >= 4 -> {
                            Toast.makeText(applicationContext(), "태그는 최대 4개까지 등록 가능합니다.", Toast.LENGTH_SHORT).show()
                            binding.inputTag.setText(tagName)
                            binding.inputTag.setSelection(binding.inputTag.text.length)
                        }

                        // 태그명 중복된 경우, 나머지는 정상 처리
                        else -> {
                            val candidateTags = tagViewModel.candidateTags
                            if(tagName !in candidateTags.map{ it.name } ) {
                                candidateTags.add(Tag(tagName))
                                makeChip(tagName, binding.flexBoxTagEnrolled)
                                Toast.makeText(applicationContext(), "\'$tagName\' 저장", Toast.LENGTH_SHORT).show()
                                binding.inputTag.text.clear()
                            } else {
                                Toast.makeText(applicationContext(), "태그명이 중복되었습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                                binding.inputTag.setText(tagName)
                                binding.inputTag.setSelection(binding.inputTag.text.length)
                            }
                        }
                    }
                }
            }
        }

        private fun isLastWordBlank(s: CharSequence): Boolean =
            s[s.length - 1].toInt() == ' '.toInt()

    }
}