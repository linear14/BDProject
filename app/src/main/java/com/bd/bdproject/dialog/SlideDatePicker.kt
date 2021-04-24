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

class SlideDatePicker(val dateBundle: Bundle?, val listener: DatePickerDialog.OnDateSetListener) : DialogFragment() {

    companion object {
        const val MAX_YEAR = 2099
        const val MIN_YEAR = 1980
    }

    private var _binding: DialogThreeItemPickerBinding? = null
    val binding get() = _binding!!

    private val cal: Calendar = Calendar.getInstance()

    init {
        dateBundle?.let {
            Log.d("PICKER_TEST", "${it.getInt("YEAR")}년 ${it.getInt("MONTH")}월 ${it.getInt("DATE")}일")
            cal.set(it.getInt("YEAR"), it.getInt("MONTH") - 1, it.getInt("DATE"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogThreeItemPickerBinding.inflate(LayoutInflater.from(context))

        _binding?.let {
            it.apply {

                pickerFirst.minValue = MIN_YEAR
                pickerFirst.maxValue = MAX_YEAR
                pickerFirst.value = cal[Calendar.YEAR]

                pickerSecond.minValue = 1
                pickerSecond.maxValue = 12
                pickerSecond.value = cal[Calendar.MONTH] + 1

                pickerThird.minValue = 1
                pickerThird.maxValue = 31
                pickerThird.value = cal[Calendar.DATE]

                setDialogTitle()

                btnConfirm.setOnClickListener {
                    listener.onDateSet(null, pickerFirst.value, pickerSecond.value, pickerThird.value)
                    dismiss()
                }

                btnCancel.setOnClickListener {
                    dismiss()
                }
            }
        }

        setDatePickerValueChangedListener()

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

    private fun setDatePickerValueChangedListener() {
        binding.apply {
            pickerFirst.setOnValueChangedListener { picker, oldVal, newVal ->
                cal.set(Calendar.YEAR, newVal)
                cal.set(Calendar.DAY_OF_MONTH, 1)

                val newMaxDate = cal.getActualMaximum(Calendar.DATE)
                if(newMaxDate < pickerThird.value) {
                    pickerThird.value = 1
                }
                pickerThird.maxValue = newMaxDate
                setDialogTitle()
            }

            pickerSecond.setOnValueChangedListener { picker, oldVal, newVal ->
                cal.set(Calendar.MONTH, newVal - 1)
                cal.set(Calendar.DAY_OF_MONTH, 1)

                val newMaxDate = cal.getActualMaximum(Calendar.DATE)
                if(newMaxDate < pickerThird.value) {
                    pickerThird.value = 1
                }
                pickerThird.maxValue = newMaxDate
                setDialogTitle()
            }

            pickerThird.setOnValueChangedListener { picker, oldVal, newVal ->
                setDialogTitle()
            }

        }
    }

    private fun setDialogTitle() {

        val noMatchedDate = "존재하지 않는 날짜"

        binding.apply {
            val year = pickerFirst.value
            val month = pickerSecond.value
            val date = pickerThird.value

            val titleCal = Calendar.getInstance()
            titleCal.set(year, month - 1, date)

            val day = when(titleCal[Calendar.DAY_OF_WEEK]) {
                1 -> "일"
                2 -> "월"
                3 -> "화"
                4 -> "수"
                5 -> "목"
                6 -> "금"
                7 -> "토"
                else -> noMatchedDate
            }


            tvCurrentState.text = if(day != noMatchedDate) {
                "${year}년 ${month}월 ${date}일 (${day})"
            } else {
                day
            }
        }
    }

}