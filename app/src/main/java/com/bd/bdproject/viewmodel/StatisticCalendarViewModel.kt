package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.StatisticViewType
import com.bd.bdproject.data.model.StatisticCalendar
import java.util.*

class StatisticCalendarViewModel: ViewModel() {

    val calendarList: MutableLiveData<MutableList<StatisticCalendar>> = MutableLiveData()

    val duration: MutableLiveData<Pair<Long?, Long?>> = MutableLiveData()
    val centerPosition: MutableLiveData<Int> = MutableLiveData()

    init {
        getCalendarList()
    }

    private fun getCalendarList() {
        val cal = GregorianCalendar()
        val tempCalendarList = mutableListOf<StatisticCalendar>()

        for(i in -120 .. 0) {
            // 세팅을 매 달 1일로 맞춰둠
            val calendar = GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + i, 1, 0, 0, 0)
            if(i == -1) {
                centerPosition.value = tempCalendarList.size    // 이전 달을 맨 위로 보이도록 설정
            }
            tempCalendarList.add(StatisticCalendar(StatisticViewType.CALENDAR_HEADER, calendar.timeInMillis))

            // 해당 월 1일의 (EX 3월 1일) 요일. 1을 뺐으므로 비어있는 칸이 어디까지인지 알 수 있음
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1

            // 해당 월의 마지막 일자
            val maxOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            for(j in 0 until dayOfWeek) {
                tempCalendarList.add(StatisticCalendar(StatisticViewType.CALENDAR_EMPTY))
            }

            for(j in 1..maxOfMonth) {
                calendar.set(Calendar.DAY_OF_MONTH, j)
                tempCalendarList.add(StatisticCalendar(StatisticViewType.CALENDAR_DAY, calendar.timeInMillis))
            }
        }

        calendarList.value = tempCalendarList
    }
}