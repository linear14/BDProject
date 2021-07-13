package com.bd.bdproject.dialog

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.bd.bdproject.databinding.DialogBitdamBinding

class BitdamDialog(val msg: String, val onPositiveSelected: () -> Unit): DialogFragment() {

    private var _binding: DialogBitdamBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBitdamBinding.inflate(inflater, container, false)
        isCancelable = false

        binding.apply {
            tvDescription.text = msg

            btnYes.setOnClickListener {
                onPositiveSelected.invoke()
                dismiss()
            }
            btnNo.setOnClickListener { dismiss() }
            btnClose.setOnClickListener { dismiss() }
        }

        dialog?.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val windowWidth = Resources.getSystem().displayMetrics.widthPixels

        val params = dialog?.window?.attributes
        params?.let {
            it.width = (windowWidth * 0.666).toInt()
        }
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}