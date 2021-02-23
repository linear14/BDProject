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
import com.bd.bdproject.util.toLightLabel
import com.bd.bdproject.util.withDateSeparator
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

    private fun observeDate() {
        statisticViewModel.duration.observe(this) {
            val startDay = it.first.timeToString()
            val endDay = it.second.timeToString()

            BitdamLog.titleLogger("옵저버를 통해 관찰된 결과 (observeDate())")
            BitdamLog.dateCodeLogger(startDay)
            BitdamLog.dateCodeLogger(endDay)

            if(startDay == endDay) {
                binding.btnDuration.text = "${startDay.withDateSeparator(".")}"
            } else {
                binding.btnDuration.text = "${startDay.withDateSeparator(".")} - ${endDay.withDateSeparator(".")}"
            }
            statisticViewModel.getLightWithTagsForDuration()
        }
    }

    private fun observeLightForDuration() {
        statisticViewModel.lightForDuration.observe(this) {
            val entry = statisticViewModel.makePieChartEntry(it)
            binding.chartLight.setData(entry)

            val tagStatistic: MutableList<StatisticTagResult> =
                statisticViewModel.makeTagStatistic(it).apply {
                sortByDescending { tags -> tags.cnt }
            }

            statisticTagAdapter.submitList(tagStatistic)
        }
    }

}