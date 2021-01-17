package com.bd.bdproject.view.fragment

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.bd.bdproject.databinding.FragmentControlMemoBinding
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.view.adapter.TagAdapter
import com.bd.bdproject.viewmodel.AddViewModel
import com.bd.bdproject.viewmodel.common.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.common.LightViewModel
import com.bd.bdproject.viewmodel.common.TagViewModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import gun0912.tedkeyboardobserver.TedKeyboardObserver
import org.koin.android.ext.android.inject

open class ControlMemoFragment: BaseFragment() {

    private var _binding: FragmentControlMemoBinding? = null
    val binding get() = _binding!!

    val tagEnrolledAdapter by lazy { TagAdapter() }

    val gradientDrawable = GradientDrawable().apply {
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
        _binding = FragmentControlMemoBinding.inflate(inflater, container, false).apply {

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

        binding.inputMemo.addTextChangedListener(InputMemoWatcher())
        observeKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    fun setEntireMemoFragmentColor(brightness: Int) {
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