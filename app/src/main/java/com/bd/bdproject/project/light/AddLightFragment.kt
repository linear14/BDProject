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
import com.bd.bdproject.BitDamApplication.Companion.applicationContext
import com.bd.bdproject.R
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.FragmentAddLightBinding
import com.bd.bdproject.util.animateTransparency
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
        }
        showUiWithDelay()

        setSeekBarPressListener()
        setSeekBarProgressChangedListener()
        setSeekBarReleaseListener()

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
            }
        }
    }

    private fun getBrightness(step: Int): Int {
        val convertedStep = step / 10
        return (convertedStep * 5)
    }

    inner class InputTagWatcher: TextWatcher {

        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if(s.isNotEmpty()) {
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
                                makeChip(tagName)
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

        private fun makeChip(name: String): Chip {
            val nameWithHash = "# $name"
            return Chip(requireActivity()).apply {
                text = nameWithHash
                setTextAppearanceResource(R.style.ChipTextStyle)
            }.also {
                binding.flexBoxTagEnrolled.addView(it)
                (it.layoutParams as ViewGroup.MarginLayoutParams).updateMargins(
                    left = it.context.resources.getDimensionPixelSize(R.dimen.chip_margin),
                    right = it.context.resources.getDimensionPixelSize(R.dimen.chip_margin)
                )
            }
        }

        private fun isLastWordBlank(s: CharSequence): Boolean =
            s[s.length - 1].toInt() == ' '.toInt()

    }
}