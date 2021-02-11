package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.BitdamLog
import com.bd.bdproject.StatisticViewType
import com.bd.bdproject.data.model.StatisticCalendar
import com.bd.bdproject.util.timeToString
import java.util.*

class StatisticCalendarViewModel: ViewModel() {

    val calendarList: MutableLiveData<MutableList<StatisticCalendar>> = MutableLiveData()

    val duration: MutableLiveData<Pair<Long?, Long?>> = MutableLiveData()
    val centerPosition: MutableLiveData<Int> = MutableLiveData()

    fun getCalendarList() {
        val startDay = duration.value?.first

        val cal = GregorianCalendar()
        val tempCalendarList = mutableListOf<StatisticCalendar>()

        for(i in -120 .. 0) {
            // 세팅을 매 달 1일로 맞춰둠
            val calendar = GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + i, 1, 0, 0, 0)

            // 시작 월을 달력 열었을 때 보이도록 설정
            if(startDay != null) {
                startDay.let {
                    val time = calendar.timeInMillis
                    val nextMonthCalendar = GregorianCalendar().apply {
                        timeInMillis = time
                    }.also { newCal ->
                        newCal.add(Calendar.MONTH, 1)
                    }

                    if(it in calendar.timeInMillis until nextMonthCalendar.timeInMillis) {
                        centerPosition.value = tempCalendarList.size
                    }
                }
            } else {
                if(i == -1) {
                    centerPosition.value = tempCalendarList.size
                }
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