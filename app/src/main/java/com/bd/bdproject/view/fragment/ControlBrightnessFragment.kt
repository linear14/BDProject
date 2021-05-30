package com.bd.bdproject.view.fragment

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bd.bdproject.databinding.FragmentControlBrightnessBinding
import com.bd.bdproject.util.ColorUtil.setEntireViewColor

open class ControlBrightnessFragment: BaseFragment() {

    var _binding: FragmentControlBrightnessBinding? = null
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentControlBrightnessBinding.inflate(inflater, container, false).apply {

        }

        return binding.root
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

    fun getBrightness(progress: Int): Int {
        val converted = progress / 10
        return (converted * 5)
    }

    fun setEntireLightFragmentColor(brightness: Int) {
        binding.apply {
            setEntireViewColor(
                brightness,
                tvBrightness,
                actionEnroll,
                btnDrawer,
                btnBack
            )
        }
    }


    /***
     * TODO 비동기로 2~3초마다 sbLight Thumb가 visible한지 검사하는 메서드를 만들어준다.
     * visible하면 return
     * 아니면 보여주는 메서드
     */
}