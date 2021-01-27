package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.View
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import com.bd.bdproject.util.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.util.LightUtil.getDiagonalLight
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.view.fragment.ControlBrightnessFragment
import com.bd.bdproject.viewmodel.EnrollViewModel

open class EnrollBrightnessFragment: ControlBrightnessFragment() {

    private val sharedViewModel: EnrollViewModel by activityViewModels()

    val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setSeekBarReleaseListener()
        }

    }

    override fun onResume() {
        super.onResume()

        showUi()

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
    }

    private fun showUi() {
        binding.apply {
            val brightness = sharedViewModel.brightness.value?:0

            setEntireLightFragmentColor(brightness)
            gradientDrawable.colors = getDiagonalLight(brightness * 2)
            layoutAddLight.background = gradientDrawable
            tvBrightness.text = brightness.toString()
            tvBrightness.visibility = View.VISIBLE
            sbLight.barWidth = 4
            sbLight.progress = brightness * 2

            if(sharedViewModel.brightness.value != null) {
                sbLight.animateTransparency(1.0f, 2000)
            } else {
                sbLight.alpha = 1f
            }
        }
    }

    private fun setSeekBarReleaseListener() {
        binding.apply {
            sbLight.setOnReleaseListener { progress ->
                if(!isFirstPressed && !isChangingFragment) {
                    isChangingFragment = true
                    saveBrightness()
                    goToFragmentEnrollTag()
                }
            }
        }
    }

    private fun saveBrightness() {
        sharedViewModel.brightness.value = binding.tvBrightness.text.toString().toInt()
    }

    private fun goToFragmentEnrollTag() {
        binding.sbLight.animateTransparency(0.0f, 2000)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    sharedViewModel.previousPage.value = CONTROL_BRIGHTNESS
                    parentActivity.binding.drawer.closeDrawer(GravityCompat.START)
                    val navDirection: NavDirections =
                        EnrollBrightnessFragmentDirections.actionEnrollBrightnessFragmentToEnrollTagFragment()
                    findNavController(binding.sbLight).navigate(navDirection)
                }
            })
    }
}