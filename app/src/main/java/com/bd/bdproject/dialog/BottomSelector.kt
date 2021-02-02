package com.bd.bdproject.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bd.bdproject.`interface`.OnBottomOptionSelectedListener
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.DialogBottomSelectorBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSelector(val tag: Tag): BottomSheetDialogFragment() {

    private var _binding: DialogBottomSelectorBinding? = null
    val binding get() = _binding!!

    var optionsSelectedListener: OnBottomOptionSelectedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBottomSelectorBinding.inflate(inflater, container, false)

        binding.apply {
            actionEdit.setOnClickListener { optionsSelectedListener?.onEdit(tag) }
            actionDelete.setOnClickListener { optionsSelectedListener?.onDelete(tag) }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setOnSelectedListener(li: OnBottomOptionSelectedListener) {
        optionsSelectedListener = li
    }
}