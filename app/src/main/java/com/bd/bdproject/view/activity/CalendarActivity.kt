package com.bd.bdproject.view.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.BitdamLog
import com.bd.bdproject.databinding.ActivityCalendarBinding
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.view.adapter.StatisticCalendarAdapter
import com.bd.bdproject.viewmodel.StatisticCalendarViewModel
import org.koin.android.ext.android.inject
import java.lang.StringBuilder

class CalendarActivity : AppCompatActivity() {

    lateinit var binding: ActivityCalendarBinding
    private val viewModel: StatisticCalendarViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViewModel()

        observeDuration()
        observeCalendarList()

        binding.apply {
            btnBack.setOnClickListener { finish() }
            actionConfirm.setOnClickListener {
                val resultIntent = Intent()

                val startDay = viewModel.duration.value?.first
                val endDay = viewModel.duration.value?.second
                resultIntent.putExtra("START_DAY", startDay)
                resultIntent.putExtra("END_DAY", endDay)

                BitdamLog.titleLogger("달력에서의 ViewModel 제공 데이터 (initViewModel)")
                BitdamLog.dateCodeLogger(startDay)
                BitdamLog.dateCodeLogger(endDay)

                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun getDurationText(start: Long?, end: Long?): String {
        val sb = StringBuilder()

        val newStart = start!!.timeToString()
        sb.apply {
            append(newStart.substring(0, 4))
            append(".")
            append(newStart.substring(4, 6))
            append(".")
            append(newStart.substring(6, 8))
        }

        if(end != null) {
            val newEnd = end.timeToString()
            sb.apply {
                append(" - ")
                append(newEnd.substring(0, 4))
                append(".")
                append(newEnd.substring(4, 6))
                append(".")
                append(newEnd.substring(6, 8))
            }
        }
        return sb.toString()
    }

    private fun initViewModel() {
        val startDay = intent.getLongExtra("START_DAY", System.currentTimeMillis())
        val endDay = intent.getLongExtra("END_DAY", System.currentTimeMillis())

        BitdamLog.titleLogger("달력에서의 ViewModel 제공 데이터 (initViewModel)")
        BitdamLog.dateCodeLogger(startDay)
        BitdamLog.dateCodeLogger(endDay)

        viewModel.duration.value = Pair(startDay, endDay)
        viewModel.getCalendarList()
    }

    private fun observeDuration() {
        viewModel.duration.observe(this) { duration: Pair<Long?, Long?> ->
            binding.selectorDuration.text = getDurationText(duration.first, duration.second)
        }
    }

    private fun observeCalendarList() {
        viewModel.calendarList.observe(this) { calendarList ->
            binding.apply {
                rvCalendar.layoutManager = StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL)
                rvCalendar.adapter = StatisticCalendarAdapter(calendarList).apply {
                    setViewModel(viewModel)
                }
                rvCalendar.itemAnimator = null
                viewModel.centerPosition.value?.let {
                    if(it >= 0) {
                        rvCalendar.scrollToPosition(it)
                    }
                }

            }
        }
    }

}