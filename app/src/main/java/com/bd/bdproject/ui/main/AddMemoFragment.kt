package com.bd.bdproject.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavDirections
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.bd.bdproject.BitDamApplication
import com.bd.bdproject.`interface`.OnBackPressedInFragment
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.FragmentAddMemoBinding
import com.bd.bdproject.ui.BaseFragment
import com.bd.bdproject.ui.MainActivity
import com.bd.bdproject.ui.MainActivity.Companion.LIGHT_DETAIL
import com.bd.bdproject.ui.main.adapter.TagAdapter
import com.bd.bdproject.util.*
import com.bd.bdproject.viewmodel.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.LightViewModel
import com.bd.bdproject.viewmodel.TagViewModel
import com.bd.bdproject.viewmodel.main.AddViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import gun0912.tedkeyboardobserver.TedKeyboardObserver
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import java.lang.Exception

class AddMemoFragment: BaseFragment() {

    private var _binding: FragmentAddMemoBinding? = null
    private val binding get() = _binding!!

    private val lightViewModel: LightViewModel by inject()
    private val tagViewModel: TagViewModel by inject()
    private val lightTagRelationViewModel: LightTagRelationViewModel by inject()
    private val sharedViewModel: AddViewModel by activityViewModels()

    private val args: AddMemoFragmentArgs by navArgs()

    private val tagEnrolledAdapter by lazy { TagAdapter() }

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    /***
     *  @flag
     *  - isKeyboardShowing:
     *  소프트 키보드가 보이는지 감춰져있는지 판단하는 플래그.
     *  onBackPressed를 활용하기위해 박상권님의 라이브러리를 사용했다.
     *
     *  - isChangingFragment :
     *      다음 화면으로 전환 애니메이션이 동작하면 true로 변합니다.
     *      true 상태에서는 추가적인 값의 조작이 불가능합니다.
     */
    var isKeyboardShowing: Boolean = false
    var isChangingFragment = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAddMemoBinding.inflate(inflater, container, false).apply {
            inputMemo.addTextChangedListener(InputMemoWatcher())
            actionEnroll.setOnClickListener {
                if(sharedViewModel.previousPage.value == LIGHT_DETAIL) {
                    editMemo()
                } else {
                    if(!isChangingFragment) {
                        isChangingFragment = true
                        insertLightWithTag()
                    }
                }

            }
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTagRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        isKeyboardShowing = false
        isChangingFragment = false

        observeKeyboard()
        setOnBackPressed()

        initBackground()
        showUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setOnBackPressed() {
        onBackPressedListener = object: OnBackPressedInFragment {
            override fun onBackPressed(): Boolean {
                binding.apply {
                    return if (isKeyboardShowing) {
                        KeyboardUtil.keyBoardHide(binding.inputMemo)
                        true
                    } else {
                        if(sharedViewModel.previousPage.value == LIGHT_DETAIL) {
                            return false
                        }
                        if(!isChangingFragment) {
                            saveMemo()
                            isChangingFragment = true
                            layoutMemo.animateTransparency(0.0f, 2000)
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator?) {
                                        super.onAnimationEnd(animation)
                                        sharedViewModel.previousPage.value = MainActivity.ADD_MEMO
                                        KeyboardUtil.keyBoardHide(binding.inputMemo)
                                        (activity as MainActivity).onBackPressed(true)
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

    private fun observeKeyboard() {
        TedKeyboardObserver(requireActivity()).listen { isShow ->
            isKeyboardShowing = isShow
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
                        sharedViewModel.previousPage.value = MainActivity.ADD_MEMO
                        Toast.makeText(BitDamApplication.applicationContext(), "빛 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                        val navDirection: NavDirections = AddMemoFragmentDirections.actionAddMemoFragmentToLightDetailFragment()
                        findNavController(binding.root).navigate(navDirection)
                    }
                }

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

    private fun editMemo() {
        KeyboardUtil.keyBoardHide(binding.inputMemo)
        runBlocking {
            val job = GlobalScope.launch {
                args.light?.let {
                    lightViewModel.editMemo(binding.inputMemo.text.toString(), it.dateCode)
                }
            }

            job.join()

            if(job.isCancelled) {
                Toast.makeText(BitDamApplication.applicationContext(), "메모 변경에 실패했습니다.", Toast.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    sharedViewModel.previousPage.value = MainActivity.ADD_MEMO
                    Toast.makeText(BitDamApplication.applicationContext(), "메모 변경이 완료되었습니다.", Toast.LENGTH_SHORT).show()

                    val navDirection: NavDirections = AddMemoFragmentDirections.actionAddMemoFragmentToLightDetailFragmentEdit()
                    findNavController(binding.root).navigate(navDirection)
                }
            }
        }


    }

    private fun initBackground() {
        binding.apply {
            mainActivity.binding.btnDrawer.visibility = View.GONE
            mainActivity.binding.btnBack.visibility = View.VISIBLE

            var brightness: Int? = null
            var tags: List<Tag>? = null
            var memo: String? = null

            if(sharedViewModel.previousPage.value == LIGHT_DETAIL) {
                brightness = args.light?.bright?:0
                memo = args.light?.memo
            } else {
                brightness = sharedViewModel.brightness.value?:0
                tags = sharedViewModel.tags.value?: mutableListOf()
                memo = sharedViewModel.memo.value
            }


            setEntireMemoFragmentColor(brightness)

            gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)
            layoutAddMemo.background = gradientDrawable
            inputMemo.setText(memo)

            if(sharedViewModel.previousPage.value != LIGHT_DETAIL) {
                tvBrightness.text = brightness.toString()
                tagEnrolledAdapter.submitList(tags?.toMutableList(), brightness)
            }

        }
    }

    private fun showUi() {
        if (sharedViewModel.previousPage.value != LIGHT_DETAIL) {
            CoroutineScope(Dispatchers.Main).launch {
                binding.layoutMemo.animateTransparency(1.0f, 2000)
            }
        } else {
            binding.layoutMemo.alpha = 1.0f
            binding.tvBrightness.visibility = View.GONE
        }
    }

    private fun saveMemo() {
        sharedViewModel.memo.value = binding.inputMemo.text.toString()
    }

    private fun setEntireMemoFragmentColor(brightness: Int) {
        binding.apply {
            ColorUtil.setEntireViewColor(
                brightness,
                tvBrightness,
                actionEnroll,
                inputMemo,
                tvTextCount,
                mainActivity.binding.btnBack
            )
        }
    }

    inner class InputMemoWatcher: TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            binding.apply {
                val length = s?.length?:0

                if(length > MAX_MEMO_LENGTH) {
                    inputMemo.setText(s?.substring(0, MAX_MEMO_LENGTH))
                    inputMemo.setSelection(MAX_MEMO_LENGTH)
                    Toast.makeText(requireActivity(), "메모는 ${MAX_MEMO_LENGTH}자를 넘을 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    tvTextCount.text = "${s?.length?:0}/${MAX_MEMO_LENGTH}자"
                }
            }
        }

    }

    companion object {
        const val MAX_MEMO_LENGTH = 50
    }

}