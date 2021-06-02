package com.bd.bdproject.view.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.BitdamLog
import com.bd.bdproject.R
import com.bd.bdproject.data.model.StatisticTagResult
import com.bd.bdproject.databinding.ActivityStatisticBinding
import com.bd.bdproject.common.Constant.INFO_TAG
import com.bd.bdproject.common.animateTransparency
import com.bd.bdproject.common.timeToLong
import com.bd.bdproject.common.timeToString
import com.bd.bdproject.common.withDateSeparator
import com.bd.bdproject.view.adapter.StatisticTagAdapter
import com.bd.bdproject.viewmodel.StatisticViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

        binding.apply {
            ivMyHashInfo.setOnClickListener {
                tvMyHashInfo.animateTransparency(1.0f, 500)
                .setListener(object: AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        ivMyHashInfo.setImageResource(R.drawable.ic_info_fill)
                        tvMyHashInfo.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)

                        GlobalScope.launch {
                            delay(2000)

                            GlobalScope.launch(Dispatchers.Main) {
                                tvMyHashInfo.animateTransparency(0.0f, 500)
                                    .setListener(object: AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator?) {
                                            super.onAnimationEnd(animation)
                                            ivMyHashInfo.setImageResource(R.drawable.ic_info_outline)
                                            tvMyHashInfo.visibility = View.GONE
                                        }
                                    })
                            }
                        }
                    }
                })

            }
        }

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