package com.bd.bdproject.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import com.bd.bdproject.R
import com.bd.bdproject.databinding.FragmentAddLightBinding
import com.bd.bdproject.ui.BaseFragment
import com.bd.bdproject.ui.MainActivity
import com.bd.bdproject.ui.MainActivity.Companion.ADD_LIGHT
import com.bd.bdproject.util.LightUtil.getDiagonalLight
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.viewmodel.main.AddViewModel
import kotlinx.coroutines.*

class AddLightFragment: BaseFragment() {

    private var _binding: FragmentAddLightBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: AddViewModel by activityViewModels()

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    /*** @flag
     *  - isFirstPressed :
     *      seekbar의 thumb를 클릭했는지 여부에 따라 값이 바뀝니다. (클릭하자마자 값이 바뀌는것을 방지)
     *
     *  - isChangingFragment :
     *      다음 화면으로 전환 애니메이션이 동작하면 true로 변합니다.
     *      true 상태에서는 추가적인 값의 조작이 불가능합니다.
     * ***/
    var isFirstPressed = true
    var isChangingFragment = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAddLightBinding.inflate(inflater, container, false).apply {
            sbLight.thumbPlaceholderDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.deco_seekbar_thumb)
            (activity as MainActivity).binding.btnDrawer.visibility = View.VISIBLE
            (activity as MainActivity).binding.btnBack.visibility = View.GONE
        }
        if(sharedViewModel.brightness.value == null) {
            showUiWithDelay()
        } else {
            showUi()
        }

        setSeekBarPressListener()
        setSeekBarProgressChangedListener()
        setSeekBarReleaseListener()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        isFirstPressed = true
        isChangingFragment = false

        Log.d("FLAG_STATE", "isFirstPressed: $isFirstPressed, isChangingView: $isChangingFragment")
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
                                if(!isChangingFragment) {
                                    sbLight.animateTransparency(1.0f, 2000)
                                }
                            }
                        })
                }
            }
        }
    }

    private fun showUi() {
        binding.apply {
            val brightness = sharedViewModel.brightness.value?:0
            gradientDrawable.colors = getDiagonalLight(brightness * 2)
            layoutAddLight.background = gradientDrawable
            tvBrightness.text = brightness.toString()
            tvAskCondition.visibility = View.GONE
            tvBrightness.visibility = View.VISIBLE
            sbLight.barWidth = 4
            sbLight.progress = brightness * 2
            sbLight.animateTransparency(1.0f, 2000)
        }
    }

    private fun setSeekBarPressListener() {
        binding.apply {
            sbLight.setOnPressListener { progress ->
                if(isFirstPressed) {
                    tvAskCondition.visibility = View.GONE
                    tvBrightness.visibility = View.VISIBLE
                    tvBrightness.text = getBrightness(progress).toString()
                    sbLight.barWidth = 4
                }
            }
        }
    }

    private fun setSeekBarProgressChangedListener() {
        binding.apply {
            sbLight.setOnProgressChangeListener { progress ->
                if(!isChangingFragment) {
                    tvBrightness.text = getBrightness(progress).toString()

                    gradientDrawable.colors = getDiagonalLight(progress)
                    layoutAddLight.background = gradientDrawable
                    isFirstPressed = false
                }
            }
        }
    }

    private fun setSeekBarReleaseListener() {
        binding.apply {
            sbLight.setOnReleaseListener { progress ->
                if(!isFirstPressed && !isChangingFragment) {
                    isChangingFragment = true
                    saveBrightness()
                    goToFragmentAddLight()
                }
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

    private fun goToFragmentAddLight() {
        binding.sbLight.animateTransparency(0.0f, 2000)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    sharedViewModel.previousPage.value = ADD_LIGHT
                    (activity as MainActivity).binding.drawer.closeDrawer(GravityCompat.START)
                    val navDirection: NavDirections = AddLightFragmentDirections.actionAddLightFragmentToAddTagFragment()
                    findNavController(binding.sbLight).navigate(navDirection)
                }
            })
    }

    /***
     * TODO 비동기로 2~3초마다 sbLight Thumb가 visible한지 검사하는 메서드를 만들어준다.
     * visible하면 return
     * 아니면 보여주는 메서드
     */
}