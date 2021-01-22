package com.bd.bdproject.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.data.model.StatisticTagResult
import com.bd.bdproject.databinding.ActivityStatisticBinding
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.view.adapter.StatisticTagAdapter
import com.bd.bdproject.viewmodel.StatisticViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import org.koin.android.ext.android.inject
import java.util.*

class StatisticActivity : AppCompatActivity() {

    lateinit var binding: ActivityStatisticBinding

    private val statisticViewModel: StatisticViewModel by inject()

    private val statisticTagAdapter by lazy {
        StatisticTagAdapter { tagName ->
            startActivity(
                Intent(this, StatisticDetailActivity::class.java).apply {
                    putExtra(INFO_TAG, tagName)
                    putExtra("START_DAY", statisticViewModel.startDay.value?:System.currentTimeMillis())
                    putExtra("END_DAY", statisticViewModel.endDay.value?:System.currentTimeMillis())
                })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatisticBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        binding.btnBack.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()

        binding.rvHashTable.adapter = statisticTagAdapter
        observeDate()
        observeLightForDuration()

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
            statisticViewModel.getLightWithTagsForDuration()
        }

        statisticViewModel.endDay.observe(this) {
            binding.btnEndDay.text = it?.timeToString()
            statisticViewModel.getLightWithTagsForDuration()
        }
    }

    private fun observeLightForDuration() {
        statisticViewModel.lightForDuration.observe(this) {
            val tagStatistic: MutableList<StatisticTagResult> = statisticViewModel.makeTagStatistic(it).apply {
                sortByDescending { tags -> tags.cnt }
            }

            /*for(i in tagStatistic) {
                Log.d("LIGHT_TEST", "[# ${i.name}] 사용횟수 ${i.cnt}회, 평균밝기 ${i.avg}")
            }*/

            statisticTagAdapter.submitList(tagStatistic)
        }
    }

    companion object {
        const val START_DAY = 0
        const val END_DAY = 1
    }

}