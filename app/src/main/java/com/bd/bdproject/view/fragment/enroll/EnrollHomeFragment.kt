package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.databinding.FragmentControlHomeBinding
import com.bd.bdproject.dialog.SlideDatePicker
import com.bd.bdproject.util.Constant.COLLECTION_MAIN
import com.bd.bdproject.util.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.util.Constant.CONTROL_HOME
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.SharedUtil.isAnimationActive
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.util.screenTransitionAnimationMilliSecond
import com.bd.bdproject.util.timeToLong
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import com.bd.bdproject.viewmodel.EnrollViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class EnrollHomeFragment: BaseFragment() {

    private var _binding: FragmentControlHomeBinding? = null
    val binding get() = _binding!!

    private val sharedViewModel: EnrollViewModel by activityViewModels()
    private val checkEnrollStateViewModel: CheckEnrollStateViewModel by inject()

    private val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlHomeBinding.inflate(inflater, container, false)

        parentActivity.updateBackgroundColor(LightUtil.getDiagonalLight(null))

        when {
            // 최초 진입
            sharedViewModel.previousPage == null -> {
                if(isAnimationActive()) {
                    showUiWithAnimation()
                } else {
                    showUiWithoutAnimation()
                }
            }

            // 다음 페이지에서 돌아왔을 경우
            sharedViewModel.previousPage == CONTROL_BRIGHTNESS -> {
                if(isAnimationActive()) {
                    showUiWithAnimationFromNextPage()
                } else {
                    showUiWithoutAnimation()
                }
            }

            // 기타 상황
            else -> {
                showUiWithoutAnimation()
            }
        }

        sharedViewModel.previousPage = CONTROL_HOME

        binding.apply {
            btnDrawer.setOnClickListener {
                if (!sharedViewModel.isFragmentTransitionState) {
                    parentActivity.binding.drawer.openDrawer(GravityCompat.START)
                }
            }

            actionDatePick.setOnClickListener {
                val dateBundle = Bundle().apply {
                    val dateCode = sharedViewModel.dateCode
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
                                    sharedViewModel.dateCode = sb.toString()
                                    binding.apply {
                                        goToFragmentEnrollBrightness()
                                    }
                                }
                            }
                        }
                    }
                }
                picker.show(requireActivity().supportFragmentManager, "SlideDatePicker")
            }
        }

        handleSeekBar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel.previousActivity = parentActivity.previousActivity
        if(sharedViewModel.previousActivity == COLLECTION_MAIN) {
            sharedViewModel.dateCode = parentActivity.intent.getStringExtra("datecode")
        }
    }

    override fun onResume() {
        super.onResume()
        // 다시 진입하면 데이터 초기화
        sharedViewModel.init()
    }

    private fun showUiWithAnimation() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.apply {
                sbLight.alpha = 0.0f

                tvAskCondition.clearAnimation()
                sbLight.clearAnimation()
                actionDatePick.clearAnimation()

                delay(1000)

                // 질문 메세지 애니메이션
                tvAskCondition.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                    .setListener(object: AnimatorListenerAdapter() {

                        // SeekBar 및 데이터피커 애니메이션
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            sbLight.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                                .setListener(object: AnimatorListenerAdapter() {})
                            actionDatePick.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                                .setListener(object: AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator?) {
                                        super.onAnimationEnd(animation)
                                        sharedViewModel.isFragmentTransitionState = false
                                    }
                                })
                        }
                    })
            }
        }

    }

    private fun showUiWithAnimationFromNextPage() {
        CoroutineScope(Dispatchers.Main).launch {
            sharedViewModel.isFragmentTransitionState = true
            binding.apply {
                tvAskCondition.clearAnimation()
                sbLight.clearAnimation()
                actionDatePick.clearAnimation()

                // 질문 메세지 애니메이션
                tvAskCondition.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                    .setListener(object: AnimatorListenerAdapter() {})
                actionDatePick.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
                    .setListener(object: AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            super.onAnimationEnd(animation)
                            sharedViewModel.isFragmentTransitionState = false
                        }
                    })

            }
        }
    }

    private fun showUiWithoutAnimation() {
        binding.apply {
            tvAskCondition.alpha = 1.0f
            sbLight.alpha = 1.0f
            actionDatePick.alpha = 1.0f
            sharedViewModel.isFragmentTransitionState = false
        }
    }

    private fun handleSeekBar() {
        binding.apply {
            sbLight.setOnReleaseListener {
                if(!sharedViewModel.isFragmentTransitionState) {
                    if(isAnimationActive()) {
                        goToFragmentEnrollBrightnessWithAnimation()
                    } else {
                        goToFragmentEnrollBrightness()
                    }
                }
            }
        }

    }

    private fun goToFragmentEnrollBrightnessWithAnimation() {
        binding.apply {
            sharedViewModel.isFragmentTransitionState = true
            tvAskCondition.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter() {})

            actionDatePick.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        goToFragmentEnrollBrightness()
                    }
                })
        }
    }

    // findNavController 부분 코드가 빠르게 두 번 이상 호출되면
    // java.lang.IllegalArgumentException: navigation destination xx is unknown to this NavController 뜬다.
    // https://stackoverflow.com/questions/51060762/illegalargumentexception-navigation-destination-xxx-is-unknown-to-this-navcontr
    private fun goToFragmentEnrollBrightness() {
        sharedViewModel.isFragmentTransitionState = true
        sharedViewModel.previousPage = CONTROL_HOME
        parentActivity.binding.drawer.closeDrawer(GravityCompat.START)
        val navDirection: NavDirections =
            EnrollHomeFragmentDirections.actionEnrollHomeFragmentToEnrollBrightnessFragment()
        findNavController(parentActivity.binding.layoutFragment).navigate(navDirection)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}