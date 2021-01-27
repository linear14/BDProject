package com.bd.bdproject.dialog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.bd.bdproject.databinding.DialogDatePickerBinding
import java.util.*

class SlideDatePicker(val dateBundle: Bundle?, val listener: DatePickerDialog.OnDateSetListener) : DialogFragment() {

    companion object {
        const val MAX_YEAR = 2099
        const val MIN_YEAR = 1980
    }

    private var _binding: DialogDatePickerBinding? = null
    val binding get() = _binding!!

    private val cal: Calendar = Calendar.getInstance()

    init {
        dateBundle?.let {
            Log.d("PICKER_TEST", "${it.getInt("YEAR")}년 ${it.getInt("MONTH")}월 ${it.getInt("DATE")}일")
            cal.set(it.getInt("YEAR"), it.getInt("MONTH") - 1, it.getInt("DATE"))
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogDatePickerBinding.inflate(LayoutInflater.from(context))

        _binding?.let {
            it.apply {

                pickerYear.minValue = MIN_YEAR
                pickerYear.maxValue = MAX_YEAR
                pickerYear.value = cal[Calendar.YEAR]

                pickerMonth.minValue = 1
                pickerMonth.maxValue = 12
                pickerMonth.value = cal[Calendar.MONTH] + 1

                pickerDate.minValue = 1
                pickerDate.maxValue = 31
                pickerDate.value = cal[Calendar.DATE]

                setDialogTitle()

                btnConfirm.setOnClickListener {
                    listener.onDateSet(null, pickerYear.value, pickerMonth.value, pickerDate.value)
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

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setDatePickerValueChangedListener() {
        binding.apply {
            pickerYear.setOnValueChangedListener { picker, oldVal, newVal ->
                cal.set(Calendar.YEAR, newVal)
                cal.set(Calendar.DAY_OF_MONTH, 1)

                val newMaxDate = cal.getActualMaximum(Calendar.DATE)
                if(newMaxDate < pickerDate.value) {
                    pickerDate.value = 1
                }
                pickerDate.maxValue = newMaxDate
                setDialogTitle()
            }

            pickerMonth.setOnValueChangedListener { picker, oldVal, newVal ->
                cal.set(Calendar.MONTH, newVal - 1)
                cal.set(Calendar.DAY_OF_MONTH, 1)

                val newMaxDate = cal.getActualMaximum(Calendar.DATE)
                if(newMaxDate < pickerDate.value) {
                    pickerDate.value = 1
                }
                pickerDate.maxValue = newMaxDate
                setDialogTitle()
            }

            pickerDate.setOnValueChangedListener { picker, oldVal, newVal ->
                setDialogTitle()
            }

        }
    }

    private fun setDialogTitle() {

        val noMatchedDate = "존재하지 않는 날짜"

        binding.apply {
            val year = pickerYear.value
            val month = pickerMonth.value
            val date = pickerDate.value

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