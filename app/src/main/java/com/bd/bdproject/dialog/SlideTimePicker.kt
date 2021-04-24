package com.bd.bdproject.dialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
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

    companion object {
        const val MAX_YEAR = 2099
        const val MIN_YEAR = 1980
    }

    private var _binding: DialogThreeItemPickerBinding? = null
    val binding get() = _binding!!


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogThreeItemPickerBinding.inflate(LayoutInflater.from(context))

        _binding?.let {
            it.apply {

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

                setDialogTitle()

                btnConfirm.setOnClickListener {
                    listener.invoke(pickerFirst.value, pickerSecond.value, pickerThird.value)
                    dismiss()
                }

                btnCancel.setOnClickListener {
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
            it.width = (windowWidth * 0.788).toInt()
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
                setDialogTitle()
            }

            pickerSecond.setOnValueChangedListener { picker, oldVal, newVal ->
                setDialogTitle()
            }

            pickerThird.setOnValueChangedListener { picker, oldVal, newVal ->
                setDialogTitle()
            }

        }
    }

    private fun setDialogTitle() {

        val noMatchedTime = "존재하지 않는 시간"

        binding.apply {
            val hour = pickerFirst.value
            val min = if(pickerSecond.value < 10) "0${pickerSecond.value}" else "${pickerSecond.value}"
            val ap = if(pickerThird.value == 0) "am" else "pm"

            tvCurrentState.text = "$hour : $min : $ap"
        }
    }

}