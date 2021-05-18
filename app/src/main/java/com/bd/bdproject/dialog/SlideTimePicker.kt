package com.bd.bdproject.dialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.bd.bdproject.databinding.DialogThreeItemPickerBinding
import java.util.*

class SlideTimePicker(val listener: (hour: Int, min: Int, ap: Int) -> Unit) : DialogFragment() {

    private var _binding: DialogThreeItemPickerBinding? = null
    val binding get() = _binding!!

    private var isSet = false


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogThreeItemPickerBinding.inflate(LayoutInflater.from(context))

        _binding?.let {
            it.apply {

                tvDialogTitle.text = "알림 시간을 설정해주세요"

                pickerFirst.minValue = 0
                pickerFirst.maxValue = 11
                pickerFirst.value = 0

                pickerSecond.minValue = 0
                pickerSecond.maxValue = 59
                pickerSecond.value = 0

                pickerThird.minValue = 0
                pickerThird.maxValue = 1
                pickerThird.displayedValues = arrayOf("am", "pm")
                pickerThird.value = 0

                setCurrentStateText()

                btnConfirm.setOnClickListener {
                    isSet = true
                    dismiss()
                }

                btnCancel.setOnClickListener {
                    isSet = false
                    dismiss()
                }
            }
        }

        setTimePickerValueChangedListener()

        val builder = AlertDialog.Builder(requireActivity())
            .setView(binding.root)

        val dialog = builder.create()

        dialog.window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return dialog
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

    private fun setTimePickerValueChangedListener() {
        binding.apply {
            pickerFirst.setOnValueChangedListener { picker, oldVal, newVal ->
                setCurrentStateText()
            }

            pickerSecond.setOnValueChangedListener { picker, oldVal, newVal ->
                setCurrentStateText()
            }

            pickerThird.setOnValueChangedListener { picker, oldVal, newVal ->
                setCurrentStateText()
            }

        }
    }

    private fun setCurrentStateText() {

        val noMatchedTime = "존재하지 않는 시간"

        binding.apply {
            val hour = pickerFirst.value
            val min = if(pickerSecond.value < 10) "0${pickerSecond.value}" else "${pickerSecond.value}"
            val ap = if(pickerThird.value == 0) "am" else "pm"

            tvCurrentState.text = "$hour : $min : $ap"
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        if(isSet) {
            binding.apply {
                listener.invoke(pickerFirst.value, pickerSecond.value, pickerThird.value)
            }
        } else {
            listener.invoke(-1, -1, -1)
        }
        super.onDismiss(dialog)
    }

}