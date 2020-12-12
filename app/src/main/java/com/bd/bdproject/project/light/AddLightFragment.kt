package com.bd.bdproject.project.light

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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
                chipGroupTagEnrolled.visibility = View.VISIBLE
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
        /* TODO
            1. 맨 첫글자 SPACE 불가
            2. SPACE 누르자마자 CHIP 생성 및 CHIPGROUP에 넣어주기 + edittext 글자 지우기
         */
        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if(s.isNotEmpty()) {
                val lastIndex = s.length - 1
                val tagName = s.substring(0, lastIndex)

                if(s[lastIndex].toInt() == 32) {
                    tagViewModel.candidateTags.add(Tag(tagName))
                    makeChip(tagName)
                    Toast.makeText(applicationContext(), "\'$tagName\' 저장", Toast.LENGTH_SHORT).show()
                    binding.inputTag.text.clear()
                }
            }
        }

        private fun makeChip(name: String): Chip {
            val nameWithHash = "# $name"
            return Chip(requireActivity()).apply {
                text = nameWithHash
                setTextAppearanceResource(R.style.ChipTextStyle)
            }.also {
                binding.chipGroupTagEnrolled.addView(it)
            }
        }

    }
}