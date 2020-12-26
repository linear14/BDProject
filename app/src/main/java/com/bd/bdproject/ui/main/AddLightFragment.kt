package com.bd.bdproject.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import com.bd.bdproject.R
import com.bd.bdproject.databinding.FragmentAddLightBinding
import com.bd.bdproject.util.LightUtil.getDiagonalLight
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.viewmodel.main.AddViewModel
import kotlinx.coroutines.*

class AddLightFragment: Fragment() {

    private var _binding: FragmentAddLightBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: AddViewModel by activityViewModels()

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAddLightBinding.inflate(inflater, container, false).apply {
            sbLight.thumbPlaceholderDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.deco_seekbar_thumb)
            btnEnroll.setOnClickListener {
                saveBrightness()
                goToFragmentAddLight(it)
            }
        }
        showUiWithDelay()

        setSeekBarPressListener()
        setSeekBarProgressChangedListener()
        setSeekBarReleaseListener()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            sbLight.setOnPressListener { progress ->
                tvAskCondition.visibility = View.GONE
                tvBrightness.visibility = View.VISIBLE
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
            }
        }
    }

    private fun getBrightness(progress: Int): Int {
        val converted = progress / 10
        return (converted * 5)
    }

    private fun saveBrightness() {
        sharedViewModel.brightness.value = binding.tvBrightness.text.toString().toInt()
    }

    private fun goToFragmentAddLight(view: View) {
        binding.sbLight.animateTransparency(0.0f, 2000)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    val navDirection: NavDirections = AddLightFragmentDirections.actionAddLightFragmentToAddTagFragment()
                    findNavController(view).navigate(navDirection)
                }
            })
    }

    /***
     * TODO 비동기로 2~3초마다 sbLight Thumb가 visible한지 검사하는 메서드를 만들어준다.
     * visible하면 return
     * 아니면 보여주는 메서드
     */
}