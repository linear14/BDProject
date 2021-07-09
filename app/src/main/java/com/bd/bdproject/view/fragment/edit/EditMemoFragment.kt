package com.bd.bdproject.view.fragment.edit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.bd.bdproject.common.BitDamApplication
import com.bd.bdproject.common.Constant
import com.bd.bdproject.common.Constant.CONTROL_MEMO
import com.bd.bdproject.databinding.FragmentControlMemoBinding
import com.bd.bdproject.util.ColorUtil
import com.bd.bdproject.util.KeyboardUtil
import com.bd.bdproject.util.LightUtil
import com.bd.bdproject.view.activity.BitdamEditActivity
import com.bd.bdproject.view.fragment.BaseFragment
import com.bd.bdproject.viewmodel.EditViewModel
import com.bd.bdproject.viewmodel.common.LightViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

open class EditMemoFragment: BaseFragment() {

    private var _binding: FragmentControlMemoBinding? = null
    val binding get() = _binding!!
    private val editViewModel: EditViewModel by activityViewModels()

    private val parentActivity by lazy {
        activity as BitdamEditActivity
    }

    var isKeyboardShowing: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentControlMemoBinding.inflate(inflater, container, false)

        initViewAndData(editViewModel.light?.bright?:0, editViewModel.light?.memo)

        binding.apply {
            inputMemo.addTextChangedListener(InputMemoWatcher())

            actionEnroll.setOnClickListener {
                KeyboardUtil.keyBoardHide(binding.inputMemo)
                editMemo()
            }
            btnBack.setOnClickListener { parentActivity.onBackPressed() }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        isKeyboardShowing = false
    }

    private fun initViewAndData(brightness: Int, memo: String?) {
        binding.apply {
            setEntireMemoFragmentColor(brightness)
            parentActivity.updateBackgroundColor(LightUtil.getDiagonalLight(brightness * 2))

            if(memo != null && memo.isNotEmpty()) {
                inputMemo.setText(memo)
                tvTextCount.text = "${memo.length}/${Constant.MAX_MEMO_LENGTH}자"
            }

            tvBrightness.visibility = View.GONE
            rvTagEnrolled.visibility = View.GONE
            layoutMemo.alpha = 1.0f
        }
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

    private fun editMemo() {
        CoroutineScope(Dispatchers.IO).launch {
            launch {
                val light = editViewModel.light
                if(light != null) {
                    editViewModel.editMemo(binding.inputMemo.text.toString(), light.dateCode)
                }
            }.join()

            CoroutineScope(Dispatchers.Main).launch {
                parentActivity.returnToDetailActivity(CONTROL_MEMO)
            }
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

                if(length > Constant.MAX_MEMO_LENGTH) {
                    inputMemo.setText(s?.substring(0, Constant.MAX_MEMO_LENGTH))
                    inputMemo.setSelection(Constant.MAX_MEMO_LENGTH)
                    Toast.makeText(requireContext(), "메모는 ${Constant.MAX_MEMO_LENGTH}자를 넘을 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    tvTextCount.text = "${s?.length?:0}/${Constant.MAX_MEMO_LENGTH}자"
                }
                editViewModel.light = editViewModel.light?.copy(memo = inputMemo.text.toString())
            }
        }

    }
}