package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import com.bd.bdproject.common.*
import com.bd.bdproject.common.Constant.COLLECTION_MAIN
import com.bd.bdproject.common.Constant.CONTROL_BRIGHTNESS
import com.bd.bdproject.common.Constant.CONTROL_HOME
import com.bd.bdproject.databinding.FragmentControlHomeBinding
import com.bd.bdproject.dialog.SlideDatePicker
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.util.SharedUtil.isAnimationActive
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
import org.koin.androidx.viewmodel.ext.android.viewModel

class EnrollHomeFragment: BaseFragment() {

    private var _binding: FragmentControlHomeBinding? = null
    val binding get() = _binding!!

    private val sharedViewModel: EnrollViewModel by activityViewModels()
    private val checkEnrollStateViewModel: CheckEnrollStateViewModel by viewModel()

    private val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

    private var picker: SlideDatePicker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlHomeBinding.inflate(inflater, container, false)
        checkEnrollStateViewModel

        goBrightnessFragmentIfPreviousActivityIsCollectionMain()

        parentActivity.updateBackgroundColor(LightUtil.getDiagonalLight(null))
        binding.sbLight.isHome = true

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

                picker = SlideDatePicker(dateBundle) { view, year, monthOfYear, dayOfMonth ->
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
                picker?.show(requireActivity().supportFragmentManager, "SlideDatePicker")
            }
        }

        handleSeekBar()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // 다시 진입하면 데이터 초기화
        if(sharedViewModel.previousPage == CONTROL_BRIGHTNESS) {
            sharedViewModel.init()
        }
        if(BitDamApplication.pref.firstInEnrollHome) {
            binding.actionDatePick.visibility = View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
        picker?.dismissAllowingStateLoss()
    }

    private fun goBrightnessFragmentIfPreviousActivityIsCollectionMain() {
        sharedViewModel.previousActivity = parentActivity.previousActivity
        if(sharedViewModel.previousActivity == COLLECTION_MAIN) {
            val dateCode = parentActivity.intent.getStringExtra("datecode")
            if(dateCode != null) {
                sharedViewModel.dateCode = dateCode
                goToFragmentEnrollBrightness()
            } else {
                Toast.makeText(requireContext(), "날짜값이 비어있습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                parentActivity.finish()
            }
        }
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
                            if(BitDamApplication.pref.firstInEnrollHome) {
                                tooltip.show()
                                BitDamApplication.pref.firstInEnrollHome = false
                            }
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
                    sharedViewModel.dateCode = System.currentTimeMillis().timeToString()
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
                        sharedViewModel.isFragmentTransitionState = false
                    }
                })
            if(tooltip.isVisible) {
                tooltip.hide()
            }
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
        sharedViewModel.isFragmentTransitionState = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}