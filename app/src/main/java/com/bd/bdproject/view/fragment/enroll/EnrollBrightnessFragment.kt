package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.activityViewModels
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
                    /*sharedViewModel.isFragmentTransitionState = true

                    saveBrightness()

                    if(isAnimationActive()) {
                        goToFragmentEnrollTagWithAnimation()
                    } else {
                        goToFragmentEnrollTag()
                    }*/
                }
            }
        }

    }

    private fun setOnBackPressed() {
        onBackPressedListener = object: OnBackPressedInFragment {
            override fun onBackPressed(): Boolean {
                binding.apply {
                    return if(!sharedViewModel.isFragmentTransitionState) {
                        sharedViewModel.brightness.value = 0
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
        sharedViewModel.brightness.value = binding.tvBrightness.text.toString().toInt()
    }

    fun setEntireLightFragmentColor(brightness: Int) {
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
        }
    }

    private fun showUiWithAnimationFromPreviousPage() {
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
                        sbLightFake.visibility = View.GONE
                        sbLight.thumbAvailable = true
                    }
                })

            }
            sbLight.startAnimation(scaleUp)
        }
    }

    private fun showUiWithAnimationFromNextPage() {
        binding.apply {
            sbLightFake.visibility = View.GONE
            tvBrightness.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter(){
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        tvBrightness.alpha = 0f
                    }
                })
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
                    }
                })
        }
    }

    private fun goToFragmentEnrollTag() {
        saveBrightness()
    }

    private fun goToFragmentEnrollTagWithAnimation() {

    }

    private fun goBackToFragmentEnrollHome() {

    }

    private fun goBackToFragmentEnrollHomeWithAnimation() {
        binding.apply {
            tvBrightness.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter(){})
            thumbFake.visibility = View.VISIBLE
            layoutAddLight.transitionToEnd()
            // TODO sbLightFake 보여지게 바꾸기
            // TODO sbLight 막대기 아래로 내려가보이도록 크기 줄이기
            val scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.bitdam_seekbar_scale_x_down).apply {
                setAnimationListener(object: Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {
                    }

                    override fun onAnimationStart(animation: Animation?) {
                        sbLight.thumbAvailable = false
                        sbLightFake.visibility = View.VISIBLE
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
                        screenTransitionAnimationMilliSecond, time, true))
                    delay(10)
                }
            }


        }

    }

    private fun setMotionLayoutTransitionEndListener() {
        binding.apply {
            layoutAddLight.setTransitionListener(object: MotionLayout.TransitionListener {
                override fun onTransitionChange(p0: MotionLayout?, p1: Int, p2: Int, p3: Float) {}
                override fun onTransitionTrigger(p0: MotionLayout?, p1: Int, p2: Boolean, p3: Float) {}

                override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
                    TODO("Not yet implemented")
                }

                override fun onTransitionCompleted(p0: MotionLayout?, p1: Int) {
                    TODO("Not yet implemented")
                }


            })
        }
    }
}
/*
    private fun showUiWithAnimation() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                tvAskCondition.visibility = View.GONE
                tvAskCondition.clearAnimation()
                sbLight.clearAnimation()

                delay(1000)

                launch(Dispatchers.Main) {
                    tvAskCondition.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                        .setListener(object: AnimatorListenerAdapter() {
                            override fun onAnimationStart(animation: Animator?) {
                                super.onAnimationStart(animation)
                                tvAskCondition.visibility = View.VISIBLE
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                super.onAnimationEnd(animation)
                                if(!isChangingFragment) {
                                    sbLight.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                                        .setListener(object: AnimatorListenerAdapter() {
                                            override fun onAnimationStart(animation: Animator?) {
                                                super.onAnimationStart(animation)
                                                sbLight.visibility = View.VISIBLE
                                            }
                                        })
                                    actionDatePick.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                                        .setListener(object: AnimatorListenerAdapter() {
                                            override fun onAnimationStart(animation: Animator?) {
                                                actionDatePick.visibility = View.VISIBLE
                                            }
                                        })
                                }
                            }
                        })
                }
            }
        }

    }

    private fun showUiWithoutAnimation() {
        binding.apply {
            tvAskCondition.visibility = View.VISIBLE
            sbLight.visibility = View.VISIBLE
            actionDatePick.visibility = View.VISIBLE

            tvAskCondition.alpha = 1.0f
            sbLight.alpha = 1.0f
            actionDatePick.alpha = 1.0f
        }
    }

    fun makeBackground(brightness: Int) {
        binding.apply {
            setEntireLightFragmentColor(brightness)
            gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
            layoutAddLight.background = gradientDrawable
            tvBrightness.text = brightness.toString()
            tvBrightness.visibility = View.VISIBLE
            sbLight.firstProgress = brightness * 2
            sbLight.thumbAvailable = true

            actionDatePick.visibility = View.GONE
            tvAskCondition.visibility = View.GONE
            sbLight.makeBarVisible()
            isFirstPressed = false

            if(parentActivity.previousActivity == COLLECTION_MAIN) {
                btnBack.visibility = View.VISIBLE
                btnDrawer.visibility = View.GONE
            } else {
                btnBack.visibility = View.GONE
                btnDrawer.visibility = View.VISIBLE
            }

            if(isAnimationActive()) {
                if(sharedViewModel.brightness.value != null) {
                    sbLight.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                } else {
                    if(parentActivity.previousActivity == COLLECTION_MAIN) {
                        sbLight.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                    }
                }
            } else {
                sbLight.alpha = 1.0f
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

                    gradientDrawable.colors = LightUtil.getDiagonalLight(progress)
                    layoutAddLight.background = gradientDrawable
                    isFirstPressed = false
                }
            }
        }
    }


    private fun saveBrightness() {
        sharedViewModel.brightness.value = binding.tvBrightness.text.toString().toInt()
    }

    private fun goToFragmentEnrollTagWithAnimation() {
        binding.sbLight.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    goToFragmentEnrollTag()
                }
            })
    }

    private fun goToFragmentEnrollTag() {
        sharedViewModel.previousPage.value = CONTROL_BRIGHTNESS
        parentActivity.binding.drawer.closeDrawer(GravityCompat.START)
        val navDirection: NavDirections =
            EnrollBrightnessFragmentDirections.actionEnrollBrightnessFragmentToEnrollTagFragment()
        findNavController(binding.sbLight).navigate(navDirection)
    }

    private fun getBrightness(progress: Int): Int {
        val converted = progress / 10
        return (converted * 5)
    }

    fun setEntireLightFragmentColor(brightness: Int) {
        binding.apply {
            ColorUtil.setEntireViewColor(
                brightness,
                tvBrightness,
                actionEnroll,
                btnDrawer,
                btnBack
            )
        }
    }
}*/
