package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import com.bd.bdproject.databinding.FragmentControlBrightnessBinding
import com.bd.bdproject.dialog.SlideDatePicker
import com.bd.bdproject.util.*
import com.bd.bdproject.util.Constant.COLLECTION_MAIN
import com.bd.bdproject.util.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.util.SharedUtil.isAnimationActive
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.view.activity.DetailActivity
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.view.fragment.ControlBrightnessFragment
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import com.bd.bdproject.viewmodel.EnrollViewModel
import com.google.android.material.snackbar.Snackbar
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

    /*** @flag
     *  - isFirstPressed :
     *      seekbar의 thumb를 클릭했는지 여부에 따라 값이 바뀝니다. (클릭하자마자 값이 바뀌는것을 방지)
     *
     *  - isChangingFragment :
     *      다음 화면으로 전환 애니메이션이 동작하면 true로 변합니다.
     *      true 상태에서는 추가적인 값의 조작이 불가능합니다.
     * ***/
    var isFirstPressed = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentControlBrightnessBinding.inflate(inflater, container, false)

        /*// 단순 화면 전환이거나
        if (sharedViewModel.brightness.value != null || parentActivity.previousActivity == COLLECTION_MAIN) {
            makeBackground(sharedViewModel.brightness.value ?: 0)
        } else {
            if (isAnimationActive()) {
                showUiWithAnimation()
            } else {
                showUiWithoutAnimation()
            }

        }*/
        return binding.root
    }
}

/*
        binding.apply {
            btnDrawer.setOnClickListener {
                if (!sharedViewModel.isFragmentTransitionState) {
                    parentActivity.binding.drawer.openDrawer(GravityCompat.START)
                }
            }

            btnBack.setOnClickListener {
                parentActivity.onBackPressed()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.previousActivity.value = parentActivity.previousActivity
        if(sharedViewModel.previousActivity.value == COLLECTION_MAIN) {
            sharedViewModel.dateCode.value = parentActivity.intent.getStringExtra("datecode")
        }

        binding.apply {
            setSeekBarReleaseListener()
            setThumbFirstClickListener()
        }

    }

    override fun onResume() {
        super.onResume()

        isFirstPressed = true
        isChangingFragment = false

        binding.actionDatePick.setOnClickListener {
            val dateBundle = Bundle().apply {
                val dateCode = sharedViewModel.dateCode.value
                dateCode?.let {
                    putInt("YEAR", it.substring(0, 4).toInt())
                    putInt("MONTH", it.substring(4, 6).toInt())
                    putInt("DATE", it.substring(6, 8).toInt())
                }
            }

            val picker = SlideDatePicker(dateBundle) { view, year, monthOfYear, dayOfMonth ->
                Log.d("PICKER_TEST", "${year}년 ${monthOfYear}월 ${dayOfMonth}일")

                val sb = StringBuilder().apply {
                    append(year)
                    append(if(monthOfYear < 10) "0${monthOfYear}" else monthOfYear)
                    append(if(dayOfMonth < 10) "0${dayOfMonth}" else dayOfMonth)
                }

                if(sb.toString().timeToLong() > System.currentTimeMillis()) {
                    Snackbar.make(binding.root, "미래의 빛을 등록할 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val deferred = checkEnrollStateViewModel.isEnrolledTodayAsync(sb.toString())
                        val isEnrolledToday = deferred.await()

                        launch(Dispatchers.Main) {
                            if(isEnrolledToday) {
                                Snackbar.make(binding.root, "이미 빛 정보가 등록되어 있는 날입니다.", Snackbar.LENGTH_SHORT).show()
                            } else {
                                sharedViewModel.dateCode.value = sb.toString()
                                binding.apply {
                                    actionDatePick.visibility = View.GONE
                                    tvAskCondition.visibility = View.GONE
                                    tvBrightness.visibility = View.VISIBLE
                                    tvBrightness.text =
                                        (sharedViewModel.brightness.value ?: 0).toString()
                                    sbLight.makeBarVisible()
                                    sbLight.thumbAvailable = true
                                    isFirstPressed = false
                                }
                            }
                        }
                    }
                }
            }
            picker.show(requireActivity().supportFragmentManager, "SlideDatePicker")
        }
    }

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

    private fun setSeekBarReleaseListener() {
        binding.apply {
            sbLight.setOnReleaseListener {
                if(!isFirstPressed && !isChangingFragment) {
                    isChangingFragment = true
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

    private fun setThumbFirstClickListener() {
        binding.apply {
            sbLight.setOnThumbFirstClickListener {
                sbLight.thumbAvailable = true

                if (isFirstPressed) {
                    tvAskCondition.visibility = View.GONE
                    tvBrightness.visibility = View.VISIBLE
                    actionDatePick.visibility = View.GONE

                    sbLight.makeBarVisible()
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
