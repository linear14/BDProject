package com.bd.bdproject.ui.main

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bd.bdproject.`interface`.JobFinishedListener
import com.bd.bdproject.databinding.FragmentAddMemoBinding
import com.bd.bdproject.ui.main.adapter.TagAdapter
import com.bd.bdproject.viewmodel.LightTagRelationViewModel
import com.bd.bdproject.viewmodel.LightViewModel
import com.bd.bdproject.viewmodel.TagViewModel
import org.koin.android.ext.android.inject

class AddMemoFragment: Fragment() {

    private var _binding: FragmentAddMemoBinding? = null

    private val binding get() = _binding!!

    private val lightViewModel: LightViewModel by inject()
    private val tagViewModel: TagViewModel by inject()
    private val lightTagRelationViewModel: LightTagRelationViewModel by inject()

    private var tagEnrolledAdapter = TagAdapter()
    private var tagRecommendAdapter = TagAdapter()

    private val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }

    private var jobFinishedListener: JobFinishedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAddMemoBinding.inflate(inflater, container, false).apply {
            inputMemo.addTextChangedListener(InputMemoWatcher())
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                    Toast.makeText(requireActivity(), "메모는 80자를 넘을 수 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    tvTextCount.text = "${s?.length?:0}/80자"
                }
            }
        }

    }

    companion object {
        val MAX_MEMO_LENGTH = 80
    }

}