package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
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
import com.bd.bdproject.view.fragment.ControlBrightnessFragment
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import com.bd.bdproject.viewmodel.EnrollViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

open class EnrollBrightnessFragment: ControlBrightnessFragment() {


    private val sharedViewModel: EnrollViewModel by activityViewModels()
    private val checkEnrollStateViewModel: CheckEnrollStateViewModel by inject()

    val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentControlBrightnessBinding.inflate(inflater, container, false).apply {

        }

        if(sharedViewModel.brightness.value != null || parentActivity.previousActivity == COLLECTION_MAIN) {
            makeBackground(sharedViewModel.brightness.value?:0)
        } else {
            if(isAnimationActive()) {
                showUiWithAnimation()
            } else {
                showUiWithoutAnimation()
            }

        }

        binding.btnDrawer.setOnClickListener {
            if(!isChangingFragment) {
                parentActivity.binding.drawer.openDrawer(GravityCompat.START)
            }
        }

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
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
        }

    }

    override fun onResume() {
        super.onResume()

        // 오늘의 빛이 등록되어있는지 다시 확인 (Collection 에서 오늘의 빛을 추가했을 경우를 대비)
        CoroutineScope(Dispatchers.IO).launch {
            val currentDay = System.currentTimeMillis().timeToString()
            val deferred = checkEnrollStateViewModel.isEnrolledTodayAsync(currentDay)
            val isEnrolledToday = deferred.await()

            if(isEnrolledToday) {
                launch(Dispatchers.Main) {
                    val intent: Intent =
                        Intent(requireActivity(), DetailActivity::class.java).apply {
                            putExtra(Constant.INFO_DATE_CODE, currentDay)
                            putExtra(Constant.INFO_SHOULD_HAVE_DRAWER, true)
                        }

                    startActivity(intent.apply { putExtra(Constant.INFO_PREVIOUS_ACTIVITY, Constant.SPLASH) } )
                    parentActivity.finish()
                }
            }
        }


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
                    Toast.makeText(requireActivity(), "미래의 빛을 등록할 수 없습니다.\n다른 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val deferred = checkEnrollStateViewModel.isEnrolledTodayAsync(sb.toString())
                        val isEnrolledToday = deferred.await()

                        launch(Dispatchers.Main) {
                            if(isEnrolledToday) {
                                Toast.makeText(requireActivity(), "이미 빛 정보가 등록되어 있는 날입니다.\n다른 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                            } else {
                                sharedViewModel.dateCode.value = sb.toString()
                                binding.apply {
                                    actionDatePick.visibility = View.GONE
                                    tvAskCondition.visibility = View.GONE
                                    tvBrightness.visibility = View.VISIBLE
                                    tvBrightness.text =
                                        (sharedViewModel.brightness.value ?: 0).toString()
                                    sbLight.barWidth = 4
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
                                        .setListener(object: AnimatorListenerAdapter() {
                                            override fun onAnimationStart(animation: Animator?) {
                                                super.onAnimationStart(animation)
                                                sbLight.visibility = View.VISIBLE
                                            }
                                        })
                                    actionDatePick.animateTransparency(1.0f, 2000)
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

    override fun makeBackground(brightness: Int) {
        super.makeBackground(brightness)

        binding.apply {
            actionDatePick.visibility = View.GONE
            tvAskCondition.visibility = View.GONE

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
                    sbLight.animateTransparency(1.0f, 2000)
                }
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

                    if(isAnimationActive()) {
                        goToFragmentEnrollTagWithAnimation()
                    } else {
                        goToFragmentEnrollTag()
                    }
                }
            }
        }
    }

    private fun saveBrightness() {
        sharedViewModel.brightness.value = binding.tvBrightness.text.toString().toInt()
    }

    private fun goToFragmentEnrollTagWithAnimation() {
        binding.sbLight.animateTransparency(0.0f, 2000)
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
}