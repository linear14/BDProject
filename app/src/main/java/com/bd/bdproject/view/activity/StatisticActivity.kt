package com.bd.bdproject.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.BitdamLog
import com.bd.bdproject.data.model.StatisticTagResult
import com.bd.bdproject.databinding.ActivityStatisticBinding
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.util.timeToLong
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.view.adapter.StatisticTagAdapter
import com.bd.bdproject.viewmodel.StatisticViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.model.GradientColor
import org.koin.android.ext.android.inject


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
                        statisticViewModel.duration.value?.first ?: System.currentTimeMillis()
                    )
                    putExtra(
                        "END_DAY",
                        statisticViewModel.duration.value?.second ?: System.currentTimeMillis()
                    )
                })
        }
    }

    val requestActivity: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val data = activityResult.data

            data?.let {
                val startDay = it.getLongExtra("START_DAY", System.currentTimeMillis().timeToString().timeToLong())
                val endDay = it.getLongExtra("END_DAY", System.currentTimeMillis().timeToString().timeToLong())

                BitdamLog.titleLogger("달력에서 선택된 날짜 (requestActivity)")
                BitdamLog.dateCodeLogger(startDay)
                BitdamLog.dateCodeLogger(endDay)

                statisticViewModel.duration.value = Pair(startDay, endDay)
            }?: Toast.makeText(this@StatisticActivity, "설정한 날짜 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStatisticBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        observeDate()
        observeLightForDuration()

        binding.btnBack.setOnClickListener { onBackPressed() }
    }

    override fun onResume() {
        super.onResume()

        binding.rvHashTable.adapter = statisticTagAdapter

        binding.apply {
            btnDuration.setOnClickListener {
                val intent = Intent(it.context, CalendarActivity::class.java).apply {
                    putExtra(
                        "START_DAY",
                        statisticViewModel.duration.value?.first ?: System.currentTimeMillis()
                    )
                    putExtra(
                        "END_DAY",
                        statisticViewModel.duration.value?.second ?: System.currentTimeMillis()
                    )
                }
                requestActivity.launch(intent)
            }
        }
    }

    /*private fun openDatePicker(options: Int) {
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
    }*/

    private fun observeDate() {
        statisticViewModel.duration.observe(this) {
            val startDay = it.first.timeToString()
            val endDay = it.second.timeToString()

            BitdamLog.titleLogger("옵저버를 통해 관찰된 결과 (observeDate())")
            BitdamLog.dateCodeLogger(startDay)
            BitdamLog.dateCodeLogger(endDay)

            binding.btnDuration.text = "$startDay - $endDay"
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
        //val colors = insertColors(entry)

        val dataSet = PieDataSet(entry,"").apply {
            this.gradientColors = gradientColors
            //this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        val data = PieData(dataSet).apply {
            setDrawValues(true)
            setValueFormatter(PercentFormatter(binding.chartLight))
        }

        binding.chartLight.apply {
            isDrawHoleEnabled = true
            isRotationEnabled = false
            legend.isEnabled = false
            setDrawEntryLabels(false)
            setUsePercentValues(true)
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

    /*private fun insertColors(entryList: MutableList<PieEntry>): MutableList<Int> {
        val colors = mutableListOf<Int>()
        val labelList = entryList.map { it.label }

        if (0.toLightLabel() in labelList) {
            colors.add(Color.rgb(0, 0, 0))
        }

        if (1.toLightLabel() in labelList) {
            colors.add(Color.rgb(135, 87, 76))
        }

        if (2.toLightLabel() in labelList) {
            colors.add(Color.rgb(108, 43, 22))
        }

        if (3.toLightLabel() in labelList) {
            colors.add(Color.rgb(255, 138, 0))
        }

        if (4.toLightLabel() in labelList) {
            colors.add(Color.rgb(255, 205, 77))
        }

        return colors
    }*/

    companion object {
        const val START_DAY = 0
        const val END_DAY = 1
    }

}