package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import com.bd.bdproject.R
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.databinding.FragmentControlBrightnessBinding
import com.bd.bdproject.util.*
import com.bd.bdproject.util.Constant.COLLECTION_MAIN
import com.bd.bdproject.util.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.util.Constant.CONTROL_HOME
import com.bd.bdproject.util.Constant.CONTROL_TAG
import com.bd.bdproject.util.SharedUtil.isAnimationActive
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import com.bd.bdproject.viewmodel.EnrollViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

open class EnrollBrightnessFragment: BaseFragment() {

    private var _binding: FragmentControlBrightnessBinding? = null
    val binding get() = _binding!!

    private val sharedViewModel: EnrollViewModel by activityViewModels()
    private val checkEnrollStateViewModel: CheckEnrollStateViewModel by inject()

    private val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBrightnessBinding.inflate(inflater, container, false)

        binding.tvBrightness.text = sharedViewModel.brightness.toString()
        setEntireLightFragmentColor(sharedViewModel.brightness)

        binding.sbLight.firstProgress = sharedViewModel.brightness * 2
        binding.layoutAddLight.getConstraintSet(R.id.start)?.let { set ->
            set.getConstraint(R.id.thumb_fake)?.let { item ->
                val ratio = binding.sbLight.getThumbPositionRatio(sharedViewModel.brightness * 2)
                item.layout.verticalBias = ratio
            }
        }

        when {
            // 이전 페이지에서 넘어왔을 경우
            sharedViewModel.previousPage == CONTROL_HOME -> {
                if(isAnimationActive()) {
                    showUiWithAnimationFromPreviousPage()
                } else {
                    showUiWithoutAnimation()
                }
            }

            // 다음 페이지에서 돌아왔을 경우
            sharedViewModel.previousPage == CONTROL_TAG -> {
                if(isAnimationActive()) {
                    showUiWithAnimationFromNextPage()
                } else {
                    showUiWithoutAnimation()
                }
            }

            // 모아보기 페이지에서 빛 등록을 눌렀을 경우
            sharedViewModel.previousActivity == COLLECTION_MAIN -> {
                sharedViewModel.dateCode = parentActivity.intent.getStringExtra("datecode")
                showUiWithoutAnimation()
            }

            // 기타 상황
            else -> {
                showUiWithoutAnimation()
            }
        }

        sharedViewModel.previousPage = CONTROL_BRIGHTNESS

        binding.apply {
            actionEnroll.visibility = View.GONE

            btnBack.setOnClickListener {
                parentActivity.onBackPressed()
            }
        }

        handleSeekBar()
        setOnBackPressed()

        return binding.root
    }

    private fun handleSeekBar() {
        binding.apply {
            sbLight.setOnProgressChangeListener { progress ->
                if(!sharedViewModel.isFragmentTransitionState) {
                    val brightness = progress.convertToBrightness()
                    setEntireLightFragmentColor(brightness)
                    tvBrightness.text = brightness.toString()
                    parentActivity.updateBackgroundColor(LightUtil.getDiagonalLight(progress))

                    // Fake Thumb 위치까지 함께 조정
                    layoutAddLight.getConstraintSet(R.id.start)?.let { set ->
                        set.getConstraint(R.id.thumb_fake)?.let { item ->
                            val ratio = sbLight.getThumbPositionRatio(progress)
                            item.layout.verticalBias = ratio
                        }
                    }

                }
            }

            sbLight.setOnReleaseListener {
                if(!sharedViewModel.isFragmentTransitionState) {
                    saveBrightness()

                    if(isAnimationActive()) {
                        goToFragmentEnrollTagWithAnimation()
                    } else {
                        goToFragmentEnrollTag()
                    }
                }
            }
        }

    }

    private fun setOnBackPressed() {
        onBackPressedListener = object: OnBackPressedInFragment {
            override fun onBackPressed(): Boolean {
                binding.apply {
                    return if(!sharedViewModel.isFragmentTransitionState) {
                        sharedViewModel.isFragmentTransitionState = true

                        // 모아보기에서 넘어온 경우
                        if(sharedViewModel.previousActivity == COLLECTION_MAIN) {
                            //TODO 아예 액티비티 전환

                        } else {
                            if(isAnimationActive()) {
                                goBackToFragmentEnrollHomeWithAnimation()
                            } else {
                                goBackToFragmentEnrollHome()
                            }
                        }
                        true
                    } else {
                        true
                    }
                }
            }
        }
    }

    private fun saveBrightness() {
        sharedViewModel.brightness = binding.tvBrightness.text.toString().toInt()
    }

    private fun setEntireLightFragmentColor(brightness: Int) {
        binding.apply {
            ColorUtil.setEntireViewColor(
                brightness,
                tvBrightness,
                btnBack
            )
        }
    }

    private fun showUiWithoutAnimation() {
        binding.apply {
            tvBrightness.alpha = 1.0f
            sbLight.makeBarVisible()
            sbLight.thumbAvailable = true
            sbLightFake.visibility = View.GONE
            sharedViewModel.isFragmentTransitionState = false
        }
    }

    private fun showUiWithAnimationFromPreviousPage() {
        sharedViewModel.isFragmentTransitionState = true
        binding.apply {
            // 배경 별밤색으로 바꾸기
            CoroutineScope(Dispatchers.Main).launch {
                for(time in 0..screenTransitionAnimationMilliSecond step 10) {
                    parentActivity.updateBackgroundColor(LightUtil.getOutRangeLight(
                        screenTransitionAnimationMilliSecond, time, false))
                    delay(10)
                }
            }
            tvBrightness.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter(){})

            // 막대기 애니메이션
            val scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.bitdam_seekbar_scale_x_up).apply {
                setAnimationListener(object: Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationStart(animation: Animation?) {
                        sbLight.makeBarVisible()
                        sbLight.alpha = 1.0f
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        sbLightFake.makeThumbInVisible()
                        sbLight.thumbAvailable = true
                        sharedViewModel.isFragmentTransitionState = false
                    }
                })

            }
            sbLight.startAnimation(scaleUp)
        }
    }

    private fun showUiWithAnimationFromNextPage() {
        sharedViewModel.isFragmentTransitionState = true
        binding.apply {
            sbLightFake.visibility = View.GONE
            tvBrightness.alpha = 1.0f
            sbLight.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter(){
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        sbLight.makeBarVisible()
                        sbLight.alpha = 0f
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        sbLight.thumbAvailable = true
                        sharedViewModel.isFragmentTransitionState = false
                    }
                })
        }
    }

    private fun goToFragmentEnrollTag() {
        sharedViewModel.isFragmentTransitionState = true
        binding.sbLight.thumbAvailable = false
        saveBrightness()
        sharedViewModel.previousPage = CONTROL_BRIGHTNESS
        val navDirection: NavDirections =
            EnrollBrightnessFragmentDirections.actionEnrollBrightnessFragmentToEnrollTagFragment()
        findNavController(parentActivity.binding.layoutFragment).navigate(navDirection)
        sharedViewModel.isFragmentTransitionState = false
    }

    private fun goToFragmentEnrollTagWithAnimation() {
        sharedViewModel.isFragmentTransitionState = true
        binding.sbLight.thumbAvailable = false
        binding.apply {
            sbLight.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter(){})
            thumbFake.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter(){
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        goToFragmentEnrollTag()
                    }
                })
        }

    }

    private fun goBackToFragmentEnrollHome() {
        sharedViewModel.isFragmentTransitionState = true
        binding.sbLight.thumbAvailable = false
        sharedViewModel.previousPage = CONTROL_BRIGHTNESS
        (activity as BitdamEnrollActivity).onBackPressed(true)
        sharedViewModel.isFragmentTransitionState = false
    }

    private fun goBackToFragmentEnrollHomeWithAnimation() {
        sharedViewModel.isFragmentTransitionState = true
        binding.sbLight.thumbAvailable = false
        binding.apply {
            thumbFake.visibility = View.VISIBLE
            tvBrightness.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter(){})

            layoutAddLight.transitionToEnd()
            val scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.bitdam_seekbar_scale_x_down).apply {
                fillAfter = true
                setAnimationListener(object: Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationStart(animation: Animation?) {
                        sbLight.makeThumbInVisible()
                        sbLight.thumbAvailable = false
                    }

                    override fun onAnimationEnd(animation: Animation?) {
                    }
                })

            }
            sbLight.startAnimation(scaleDown)

            // 배경 검정색으로 바꾸기
            CoroutineScope(Dispatchers.Main).launch {
                for(time in 0..screenTransitionAnimationMilliSecond step 10) {
                    parentActivity.updateBackgroundColor(LightUtil.getOutRangeLight(
                        screenTransitionAnimationMilliSecond, time, true, (sharedViewModel.brightness / 5)))
                    delay(10)
                }
                sharedViewModel.isFragmentTransitionState = false
                goBackToFragmentEnrollHome()
            }
        }

    }
}