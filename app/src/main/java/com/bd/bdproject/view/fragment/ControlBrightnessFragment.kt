package com.bd.bdproject.view.fragment

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bd.bdproject.R
import com.bd.bdproject.databinding.FragmentControlBrightnessBinding
import com.bd.bdproject.util.ColorUtil.setEntireViewColor
import com.bd.bdproject.util.LightUtil.getDiagonalLight

open class ControlBrightnessFragment: BaseFragment() {

    private var _binding: FragmentControlBrightnessBinding? = null
    val binding get() = _binding!!

    val gradientDrawable = GradientDrawable().apply {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlBrightnessBinding.inflate(inflater, container, false).apply {

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            sbLight.thumbPlaceholderDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.deco_seekbar_thumb)

            setSeekBarPressListener()
            setSeekBarProgressChangedListener()
        }

    }

    override fun onResume() {
        super.onResume()
        isFirstPressed = true
        isChangingFragment = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                    val brightness = getBrightness(progress)
                    setEntireLightFragmentColor(brightness)

                    tvBrightness.text = brightness.toString()

                    gradientDrawable.colors = getDiagonalLight(progress)
                    layoutAddLight.background = gradientDrawable
                    isFirstPressed = false
                }
            }
        }
    }

    private fun getBrightness(progress: Int): Int {
        val converted = progress / 10
        return (converted * 5)
    }

    fun setEntireLightFragmentColor(brightness: Int) {
        binding.apply {
            setEntireViewColor(
                brightness,
                tvBrightness,
                tvAskCondition,
                actionEnroll,
                btnDrawer,
                btnBack
            )
        }
    }
}