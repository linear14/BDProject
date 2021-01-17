package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.util.*
import com.bd.bdproject.util.Constant.BITDAM_ENROLL
import com.bd.bdproject.util.Constant.CONTROL_MEMO
import com.bd.bdproject.util.Constant.INFO_DATE_CODE
import com.bd.bdproject.util.Constant.INFO_PREVIOUS_ACTIVITY
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.view.activity.DetailActivity
import com.bd.bdproject.view.fragment.ControlMemoFragment
import com.bd.bdproject.viewmodel.AddViewModel
import com.bd.bdproject.viewmodel.common.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.common.LightViewModel
import com.bd.bdproject.viewmodel.common.TagViewModel
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

open class EnrollMemoFragment: ControlMemoFragment() {

    private val lightViewModel: LightViewModel by inject()
    private val tagViewModel: TagViewModel by inject()
    private val lightTagRelationViewModel: LightTagRelationViewModel by inject()
    private val sharedViewModel: AddViewModel by activityViewModels()

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

        initBackground()
        showUi()

        binding.btnBack.setOnClickListener {
            parentActivity.onBackPressed()
        }
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
                            layoutMemo.animateTransparency(0.0f, 2000)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator?) {
                                        super.onAnimationEnd(animation)
                                        sharedViewModel.previousPage.value = CONTROL_MEMO
                                        KeyboardUtil.keyBoardHide(binding.inputMemo)
                                        (activity as BitdamEnrollActivity).onBackPressed(true)
                                    }
                                })
                            true
                        } else {
                            true
                        }
                    }
                }
            }
        }
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

                        val intent = Intent(activity, DetailActivity::class.java).apply {
                            putExtra(INFO_PREVIOUS_ACTIVITY, BITDAM_ENROLL)
                            putExtra(INFO_DATE_CODE, System.currentTimeMillis().timeToString())
                            putExtra(Constant.INFO_SHOULD_HAVE_DRAWER, true)
                        }
                        startActivity(intent)
                    }
                }

                // TODO 오늘의 빛 등록을 하지 않은 상태에서 다른 날짜를 등록했을 경우 어디로 갈려나?

            }
        }

    }

    private fun insertLight(): String {
        binding.apply {
            val currentTime = System.currentTimeMillis().timeToString()
            val light = Light(
                currentTime,
                tvBrightness.text.toString().toInt(),
                inputMemo.text.toString()
            )
            lightViewModel.asyncInsertLight(light)
            return currentTime
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

    private fun initBackground() {
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

    private fun showUi() {
        CoroutineScope(Dispatchers.Main).launch {
            binding.layoutMemo.animateTransparency(1.0f, 2000)
        }
    }

    private fun saveMemo() {
        sharedViewModel.memo.value = binding.inputMemo.text.toString()
    }

}