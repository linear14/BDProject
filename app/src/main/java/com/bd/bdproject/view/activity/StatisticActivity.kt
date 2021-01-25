package com.bd.bdproject.view.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.data.model.StatisticTagResult
import com.bd.bdproject.databinding.ActivityStatisticBinding
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.view.adapter.StatisticTagAdapter
import com.bd.bdproject.viewmodel.StatisticViewModel
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.model.GradientColor
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
                    putExtra(
                        "START_DAY",
                        statisticViewModel.startDay.value ?: System.currentTimeMillis()
                    )
                    putExtra(
                        "END_DAY",
                        statisticViewModel.endDay.value ?: System.currentTimeMillis()
                    )
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

        // 이 달력에서 선택된 millisecond 값이 32400000 만큼 크다.
        val builder = MaterialDatePicker.Builder.datePicker().setSelection(
            (previousTime ?: System.currentTimeMillis()) + 32_400_000L
        )
        val picker = builder.build()
        picker.show(supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            val realTime = it - 32_400_000
            if(options == START_DAY) {
                if(it > statisticViewModel.endDay.value!!) {
                    Toast.makeText(this, "기간 범위가 잘못되었습니다. 다시 설정해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    statisticViewModel.startDay.value = realTime
                }
            } else {
                if(it < statisticViewModel.startDay.value!!) {
                    Toast.makeText(this, "기간 범위가 잘못되었습니다. 다시 설정해주세요.", Toast.LENGTH_SHORT).show()
                } else {
                    statisticViewModel.endDay.value = realTime
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
            val entry = statisticViewModel.makePieChartEntry(it)
            showPieChart(entry)

            val tagStatistic: MutableList<StatisticTagResult> =
                statisticViewModel.makeTagStatistic(it).apply {
                sortByDescending { tags -> tags.cnt }
            }

            statisticTagAdapter.submitList(tagStatistic)
        }
    }

    private fun showPieChart(entry: MutableList<PieEntry>) {
        val gradientColors = insertColorGradient(entry)

        val dataSet = PieDataSet(entry, "label").apply {
            this.gradientColors = gradientColors
            valueTextSize = 12f
        }

        val data = PieData(dataSet).apply {
            setDrawValues(true)
        }

        binding.chartLight.apply {
            isDrawHoleEnabled = true
            setHoleColor(android.R.color.transparent)
            this.data = data
            invalidate()
        }
    }

    private fun insertColorGradient(entryList: MutableList<PieEntry>): MutableList<GradientColor> {
        val gradientColors = mutableListOf<GradientColor>()
        val labelList = entryList.map { it.label }

        if("0" in labelList) {
            gradientColors.add(GradientColor(Color.rgb(0, 0, 0), Color.rgb(135, 87, 76)))
        }

        if("1" in labelList) {
            gradientColors.add(GradientColor(Color.rgb(135, 87, 76), Color.rgb(108, 43, 22)))
        }

        if("2" in labelList) {
            gradientColors.add(GradientColor(Color.rgb(108, 43, 22), Color.rgb(223, 80, 19)))
        }

        if("3" in labelList) {
            gradientColors.add(GradientColor(Color.rgb(223, 80, 19), Color.rgb(255, 138, 0)))
        }

        if("4" in labelList) {
            gradientColors.add(GradientColor(Color.rgb(255, 138, 0), Color.rgb(255, 205, 77)))
        }

        return gradientColors
    }

    companion object {
        const val START_DAY = 0
        const val END_DAY = 1
    }

}