package com.bd.bdproject.view.fragment.enroll

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.FragmentControlMemoBinding
import com.bd.bdproject.util.*
import com.bd.bdproject.util.Constant.BITDAM_ENROLL
import com.bd.bdproject.util.Constant.COLLECTION_MAIN
import com.bd.bdproject.util.Constant.CONTROL_MEMO
import com.bd.bdproject.util.Constant.CONTROL_TAG
import com.bd.bdproject.util.Constant.INFO_DATE_CODE
import com.bd.bdproject.util.Constant.INFO_PREVIOUS_ACTIVITY
import com.bd.bdproject.util.Constant.INFO_SHOULD_HAVE_DRAWER
import com.bd.bdproject.util.SharedUtil.isAnimationActive
import com.bd.bdproject.view.activity.BitdamEnrollActivity
import com.bd.bdproject.view.activity.DetailActivity
import com.bd.bdproject.view.adapter.TagAdapter
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.view.fragment.ControlMemoFragment
import com.bd.bdproject.viewmodel.EnrollViewModel
import com.bd.bdproject.viewmodel.common.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.common.LightViewModel
import com.bd.bdproject.viewmodel.common.TagViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.snackbar.Snackbar
import gun0912.tedkeyboardobserver.TedKeyboardObserver
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

open class EnrollMemoFragment: BaseFragment() {

    private var _binding: FragmentControlMemoBinding? = null
    val binding get() = _binding!!

    private val lightViewModel: LightViewModel by inject()
    private val tagViewModel: TagViewModel by inject()
    private val lightTagRelationViewModel: LightTagRelationViewModel by inject()
    private val sharedViewModel: EnrollViewModel by activityViewModels()

    private val tagEnrolledAdapter by lazy { TagAdapter() }

    private val parentActivity by lazy {
        activity as BitdamEnrollActivity
    }

    var isKeyboardShowing: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentControlMemoBinding.inflate(inflater, container, false)

        initDataAndBackground()

        when {
            // 이전 페이지에서 넘어왔을 경우
            sharedViewModel.previousPage == CONTROL_TAG -> {
                if(isAnimationActive()) {
                    showUiWithAnimationFromPreviousPage()
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
            inputMemo.addTextChangedListener(InputMemoWatcher())
            btnBack.setOnClickListener {
                KeyboardUtil.keyBoardHide(binding.inputMemo)
                isKeyboardShowing = false
                parentActivity.onBackPressed()
            }
            actionEnroll.setOnClickListener {
                insertLightWithTag()
            }
        }

        setTagRecyclerView()
        observeKeyboard()
        setOnBackPressed()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        isKeyboardShowing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // region View

    private fun initDataAndBackground() {
        binding.apply {
            val brightness = sharedViewModel.brightness
            val tags = sharedViewModel.tags
            val memo = sharedViewModel.memo

            setEntireMemoFragmentColor(brightness)
            parentActivity.updateBackgroundColor(LightUtil.getDiagonalLight(brightness * 2))
            tvBrightness.text = brightness.toString()

            setTagRecyclerView()
            tagEnrolledAdapter.submitList(tags.toMutableList(), brightness)
            inputMemo.setText(memo)
        }

    }

    private fun setTagRecyclerView() {
        val layoutManagerEnrolled = FlexboxLayoutManager(requireActivity()).apply {
            flexDirection = FlexDirection.ROW
            justifyContent = JustifyContent.CENTER
        }

        binding.rvTagEnrolled.layoutManager = layoutManagerEnrolled
        binding.rvTagEnrolled.adapter = tagEnrolledAdapter
    }

    private fun setEntireMemoFragmentColor(brightness: Int) {
        binding.apply {
            ColorUtil.setEntireViewColor(
                brightness,
                tvBrightness,
                actionEnroll,
                inputMemo,
                tvTextCount,
                btnBack
            )
        }
    }

    private fun observeKeyboard() {
        TedKeyboardObserver(requireActivity()).listen { isShow ->
            isKeyboardShowing = isShow
        }
    }

    // endregion


    // region Transition

    private fun showUiWithAnimationFromPreviousPage() {
        sharedViewModel.isFragmentTransitionState = true
        binding.layoutMemo.animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    sharedViewModel.isFragmentTransitionState = false
                }
            })

    }

    private fun showUiWithoutAnimation() {
        sharedViewModel.isFragmentTransitionState = true
        binding.layoutMemo.alpha = 1.0f
        sharedViewModel.isFragmentTransitionState = false
    }

    private fun goBackToFragmentEnrollTagWithAnimation() {
        sharedViewModel.isFragmentTransitionState = true
        KeyboardUtil.keyBoardHide(binding.inputMemo)
        binding.apply {
            layoutMemo.animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        sharedViewModel.isFragmentTransitionState = false
                        goBackToFragmentEnrollTag()
                    }
                })
        }
    }

    private fun goBackToFragmentEnrollTag() {
        sharedViewModel.isFragmentTransitionState = true
        sharedViewModel.previousPage = CONTROL_MEMO
        KeyboardUtil.keyBoardHide(binding.inputMemo)
        (activity as BitdamEnrollActivity).onBackPressed(true)
        sharedViewModel.isFragmentTransitionState = false
    }

    // endregion


    // region Logic

    private fun insertLightWithTag() {
        sharedViewModel.isFragmentTransitionState = true
        KeyboardUtil.keyBoardHide(binding.inputMemo)

        val newLight = Light(
            sharedViewModel.dateCode,
            binding.tvBrightness.text.toString().toInt(),
            binding.inputMemo.text.toString()
        )

        CoroutineScope(Dispatchers.IO).launch {
            val insertLightJob = launch { lightViewModel.insertLight(newLight) }
            val insertTagJob = launch { tagViewModel.insertTag(sharedViewModel.tags) }
            val insertRelationJob = launch { lightTagRelationViewModel.insertRelation(newLight.dateCode, sharedViewModel.tags.toMutableList()) }

            joinAll(insertLightJob, insertTagJob, insertRelationJob)

            CoroutineScope(Dispatchers.Main).launch {
                sharedViewModel.isFragmentTransitionState = false
                sharedViewModel.previousPage = CONTROL_MEMO
                Toast.makeText(BitDamApplication.applicationContext(), "빛 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                when {
                    // 모아보기 화면에서 다른날의 빛을 추가했었던 경우
                    sharedViewModel.previousActivity == COLLECTION_MAIN -> {
                        sharedViewModel.init()
                        parentActivity.finish()
                    }

                    // 메인 화면에서 오늘의 빛을 추가했던 경우
                    sharedViewModel.dateCode == System.currentTimeMillis().timeToString() -> {
                        val intent = Intent(parentActivity, DetailActivity::class.java).apply {
                            putExtra(INFO_PREVIOUS_ACTIVITY, BITDAM_ENROLL)
                            putExtra(INFO_DATE_CODE, sharedViewModel.dateCode)
                            putExtra(INFO_SHOULD_HAVE_DRAWER, true)
                        }
                        startActivity(intent)
                        parentActivity.finish()
                    }

                    // 메인 화면에서 다른 날의 빛을 추가했던 경우 (하루가 지나 날짜가 바뀌어도 요기로 들어감)
                    sharedViewModel.dateCode != System.currentTimeMillis().timeToString() -> {
                        sharedViewModel.init()
                        val navDirection: NavDirections =
                            EnrollMemoFragmentDirections.actionEnrollMemoFragmentToEnrollHomeFragment()
                        Navigation.findNavController(parentActivity.binding.layoutFragment).navigate(navDirection)

                    }
                }

            }

        }
    }

    private fun saveMemo() {
        sharedViewModel.memo = binding.inputMemo.text.toString()
    }

    private fun setOnBackPressed() {
        onBackPressedListener = object: OnBackPressedInFragment {
            override fun onBackPressed(): Boolean {
                binding.apply {
                    return if (isKeyboardShowing) {
                        KeyboardUtil.keyBoardHide(binding.inputMemo)
                        true
                    } else {
                        if(!sharedViewModel.isFragmentTransitionState) {
                            saveMemo()

                            if(isAnimationActive()) {
                                goBackToFragmentEnrollTagWithAnimation()
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

    // endregion

    inner class InputMemoWatcher: TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.apply {
                val length = s?.length?:0

                if(length > Constant.MAX_MEMO_LENGTH) {
                    inputMemo.setText(s?.substring(0, Constant.MAX_MEMO_LENGTH))
                    inputMemo.setSelection(Constant.MAX_MEMO_LENGTH)
                    Snackbar.make(binding.root, "메모는 ${Constant.MAX_MEMO_LENGTH}자를 넘을 수 없습니다.", Snackbar.LENGTH_SHORT).show()
                } else {
                    tvTextCount.text = "${s?.length?:0}/${Constant.MAX_MEMO_LENGTH}자"
                }
            }
        }

    }

}