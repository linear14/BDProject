package com.bd.bdproject.view.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.databinding.ActivityStatisticBinding
import com.bd.bdproject.util.timeToLong
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.viewmodel.StatisticViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import org.koin.android.ext.android.inject

class StatisticActivity : AppCompatActivity() {

    lateinit var binding: ActivityStatisticBinding

    val statisticViewModel: StatisticViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatisticBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
    }

    override fun onResume() {
        super.onResume()

        observeDate()

        binding.apply {
            btnStartDay.setOnClickListener {
                openDatePicker(START_DAY)
            }

            btnEndDay.setOnClickListener {
                openDatePicker(END_DAY)
            }
        }
    }

    private fun openDatePicker(options: Int) {
        val previousTime = if(options == START_DAY) {
            statisticViewModel.startDay.value
        } else {
            statisticViewModel.endDay.value
        }
        val builder = MaterialDatePicker.Builder.datePicker().setSelection(previousTime)
        val picker = builder.build()
        picker.show(supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            if(options == START_DAY) {
                if(it > statisticViewModel.endDay.value!!) {
                    Toast.makeText(this, "기간 범위가 잘못되었습니다. 다시 설정해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    statisticViewModel.startDay.value = it
                }
            } else {
                if(it < statisticViewModel.startDay.value!!) {
                    Toast.makeText(this, "기간 범위가 잘못되었습니다. 다시 설정해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    statisticViewModel.endDay.value = it
                }
            }
        }
    }

    private fun observeDate() {
        statisticViewModel.startDay.observe(this) {
            binding.btnStartDay.text = it?.timeToString()

            Log.d("DATE_RESULT", statisticViewModel.startDay.value.toString())
            Log.d("DATE_RESULT", statisticViewModel.startDay.value!!.timeToString())
            Log.d("DATE_RESULT", statisticViewModel.startDay.value!!.timeToString().timeToLong().toString())
        }

        statisticViewModel.endDay.observe(this) {
            binding.btnEndDay.text = it?.timeToString()
        }

    }

    companion object {
        const val START_DAY = 0
        const val END_DAY = 1
    }

}