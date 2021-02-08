package com.bd.bdproject.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.StatisticViewType
import com.bd.bdproject.data.model.StatisticCalendar
import com.bd.bdproject.databinding.ActivityCalendarBinding
import com.bd.bdproject.view.adapter.StatisticCalendarAdapter
import java.util.*

class CalendarActivity : AppCompatActivity() {

    lateinit var binding: ActivityCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            rvCalendar.layoutManager = StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL)
            rvCalendar.adapter = StatisticCalendarAdapter(getCalendarList())
        }
    }


    private fun getCalendarList(): MutableList<StatisticCalendar> {
        val cal = GregorianCalendar()
        val calendarList = mutableListOf<StatisticCalendar>()

        for(i in -300 .. 300) {
            // 세팅을 매 달 1일로 맞춰둠
            val calendar = GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + i, 1, 0, 0, 0)
            calendarList.add(StatisticCalendar(StatisticViewType.CALENDAR_HEADER, calendar.timeInMillis))

            // 해당 월 1일의 (EX 3월 1일) 요일. 1을 뺐으므로 비어있는 칸이 어디까지인지 알 수 있음
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

            // 해당 월의 마지막 일자
            val maxOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            for(j in 0 until dayOfWeek) {
                calendarList.add(StatisticCalendar(StatisticViewType.CALENDAR_EMPTY))
            }

            for(j in 1..maxOfMonth) {
                calendar.set(Calendar.DAY_OF_MONTH, j)
                calendarList.add(StatisticCalendar(StatisticViewType.CALENDAR_DAY, calendar.timeInMillis))
            }
        }

        return calendarList
    }
}