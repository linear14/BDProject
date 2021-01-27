package com.bd.bdproject.view.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.bd.bdproject.databinding.FragmentControlDateBinding
import com.bd.bdproject.dialog.SlideDatePicker
import com.bd.bdproject.util.animateTransparency
import com.bd.bdproject.util.timeToLong
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import com.bd.bdproject.viewmodel.EnrollViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject


class ControlDateFragment: BaseFragment() {

    private var _binding: FragmentControlDateBinding? = null
    val binding get() = _binding!!

    private val sharedViewModel: EnrollViewModel by activityViewModels()
    private val checkEnrollStateViewModel: CheckEnrollStateViewModel by inject()

    val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

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
    ): View {
        _binding = FragmentControlDateBinding.inflate(inflater, container, false).apply {

        }
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        isFirstPressed = true
        isChangingFragment = false

        showUiWithDelay()

        binding.apply {
            val params = viewThumb.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(0, 0, 0, sbLight.marginBottom)

            viewThumb.layoutParams = params
        }

        binding.viewThumb.setOnClickListener {
            goToFragmentEnrollBrightness()
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
                    GlobalScope.launch {
                        val deferred = checkEnrollStateViewModel.isEnrolledTodayAsync(sb.toString())
                        deferred.join()

                        if(deferred.getCompleted()) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireActivity(), "이미 빛 정보가 등록되어 있는 날입니다.\n다른 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                sharedViewModel.dateCode.value = sb.toString()
                                val navDirection: NavDirections =
                                    ControlDateFragmentDirections.actionControlDateFragmentToEnrollBrightnessFragment()
                                Navigation.findNavController(binding.viewThumb).navigate(navDirection)
                            }
                        }
                    }
                }
            }
            picker.show(requireActivity().supportFragmentManager, "SlideDatePicker")
        }

        binding.btnDrawer.setOnClickListener {
            parentActivity.binding.drawer.openDrawer(GravityCompat.START)
        }
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
                                    viewThumb.animateTransparency(1.0f, 2000)
                                        .setListener(object: AnimatorListenerAdapter() {
                                            override fun onAnimationStart(animation: Animator?) {
                                                super.onAnimationStart(animation)
                                                viewThumb.visibility = View.VISIBLE
                                            }
                                        })
                                }
                            }
                        })
                }
            }
        }

    }

    private fun goToFragmentEnrollBrightness() {
        val navDirection: NavDirections =
            ControlDateFragmentDirections.actionControlDateFragmentToEnrollBrightnessFragment()
        Navigation.findNavController(binding.viewThumb).navigate(navDirection)
    }

    /***
     * TODO 비동기로 2~3초마다 sbLight Thumb가 visible한지 검사하는 메서드를 만들어준다.
     * visible하면 return
     * 아니면 보여주는 메서드
     */

}