package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.util.*
import com.bd.bdproject.util.Constant.BITDAM_ENROLL
import com.bd.bdproject.util.Constant.COLLECTION_MAIN
import com.bd.bdproject.util.Constant.CONTROL_MEMO
import com.bd.bdproject.util.Constant.INFO_DATE_CODE
import com.bd.bdproject.util.Constant.INFO_PREVIOUS_ACTIVITY
import com.bd.bdproject.util.SharedUtil.isAnimationActive
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.view.activity.DetailActivity
import com.bd.bdproject.view.fragment.ControlMemoFragment
import com.bd.bdproject.viewmodel.EnrollViewModel
import com.bd.bdproject.viewmodel.common.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.common.LightViewModel
import com.bd.bdproject.viewmodel.common.TagViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

open class EnrollMemoFragment: ControlMemoFragment() {

    /*private val lightViewModel: LightViewModel by inject()
    private val tagViewModel: TagViewModel by inject()
    private val lightTagRelationViewModel: LightTagRelationViewModel by inject()
    private val sharedViewModel: EnrollViewModel by activityViewModels()

    private val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

    override fun onResume() {
        super.onResume()

        binding.actionEnroll.setOnClickListener {
            if(!isChangingFragment) {
                isChangingFragment = true
                insertLightWithTag()
            }
        }

        setOnBackPressed()

        makeBackground()
        if(isAnimationActive()) {
            showUiWithAnimation()
        } else {
            showUiWithoutAnimation()
        }

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
    }

    private fun makeBackground() {
        binding.apply {
            val brightness = sharedViewModel.brightness.value?:0
            val tags = sharedViewModel.tags.value?: mutableListOf()
            val memo = sharedViewModel.memo.value

            setEntireMemoFragmentColor(brightness)

            gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
            layoutAddMemo.background = gradientDrawable
            inputMemo.setText(memo)

            tvBrightness.text = brightness.toString()
            tagEnrolledAdapter.submitList(tags.toMutableList(), brightness)
        }
    }

    private fun showUiWithAnimation() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.layoutMemo.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
        }
    }

    private fun showUiWithoutAnimation() {
        binding.layoutMemo.alpha = 1.0f
    }

    private fun insertLightWithTag() {
        KeyboardUtil.keyBoardHide(binding.inputMemo)
        runBlocking {
            binding.apply {
                val dateCode = GlobalScope.async { insertLight() }
                val tagList = GlobalScope.async { insertTag() }
                val job = GlobalScope.launch { insertRelation(dateCode.await(), tagList.await()) }

                job.join()

                // 왜 withContext로는 안되지? --> 완료된 함수 안에서만 사용?
                if(job.isCancelled) {
                    // TODO 예외 처리를 어떻게 할까? __ 이렇게 예외 처리하는건 맞는지 알아보기
                    isChangingFragment = false
                    Toast.makeText(BitDamApplication.applicationContext(), "등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        sharedViewModel.previousPage.value = CONTROL_MEMO
                        Toast.makeText(BitDamApplication.applicationContext(), "빛 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                        // 다른 날의 빛을 CollectionActivity 에서 추가했을 경우
                        if(sharedViewModel.previousActivity.value == COLLECTION_MAIN) {
                            sharedViewModel.init()
                            parentActivity.finish()
                        } else {
                            // 그날의 빛을 등록한 경우 --> 그날의 디테일 정보로 이동 (DetailActivity)
                            if(sharedViewModel.dateCode.value == System.currentTimeMillis().timeToString()) {
                                val intent = Intent(activity, DetailActivity::class.java).apply {
                                    putExtra(INFO_PREVIOUS_ACTIVITY, BITDAM_ENROLL)
                                    putExtra(INFO_DATE_CODE, sharedViewModel.dateCode.value?:System.currentTimeMillis().timeToString())
                                    putExtra(Constant.INFO_SHOULD_HAVE_DRAWER, true)
                                }
                                startActivity(intent)
                                parentActivity.finish()
                            } else {
                                // 다른 날의 빛을 메인에서 추가했을 경우
                                sharedViewModel.init()
                                val navDirection: NavDirections =
                                    EnrollMemoFragmentDirections.actionEnrollMemoFragmentToEnrollBrightnessFragment()
                                Navigation.findNavController(binding.root).navigate(navDirection)
                            }
                        }
                    }
                }
            }
        }

    }

    private fun insertLight(): String {
        binding.apply {
            val dateCode = sharedViewModel.dateCode.value?:System.currentTimeMillis().timeToString()
            val light = Light(
                dateCode,
                tvBrightness.text.toString().toInt(),
                inputMemo.text.toString()
            )
            lightViewModel.asyncInsertLight(light)
            return dateCode
        }
    }

    private fun insertTag(): MutableList<Tag>? {
        binding.apply {
            tagViewModel.asyncInsertTag(sharedViewModel.tags.value?: mutableListOf())
        }
        return sharedViewModel.tags.value?.toMutableList()
    }

    private fun insertRelation(dateCode: String, tagList: MutableList<Tag>?) {
        lightTagRelationViewModel.insertRelation(dateCode, tagList)
    }

    private fun setOnBackPressed() {
        onBackPressedListener = object: OnBackPressedInFragment {
            override fun onBackPressed(): Boolean {
                binding.apply {
                    return if (isKeyboardShowing) {
                        KeyboardUtil.keyBoardHide(binding.inputMemo)
                        true
                    } else {
                        if(!isChangingFragment) {
                            saveMemo()
                            isChangingFragment = true

                            if(isAnimationActive()) {
                                layoutMemo.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                                    .setListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator?) {
                                            super.onAnimationEnd(animation)
                                            goBackToFragmentEnrollTag()
                                        }
                                    })
                            } else {
                                goBackToFragmentEnrollTag()
                            }
                            true
                        } else {
                            true
                        }
                    }
                }
            }
        }
    }

    private fun saveMemo() {
        sharedViewModel.memo.value = binding.inputMemo.text.toString()
    }

    private fun goBackToFragmentEnrollTag() {
        sharedViewModel.previousPage.value = CONTROL_MEMO
        KeyboardUtil.keyBoardHide(binding.inputMemo)
        (activity as BitdamEnrollActivity).onBackPressed(true)
    }*/

}