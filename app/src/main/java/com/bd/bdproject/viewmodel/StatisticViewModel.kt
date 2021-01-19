package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class StatisticViewModel: ViewModel() {

    val calendarCurrentState = GregorianCalendar()

    var startDay: MutableLiveData<Long?> = MutableLiveData()
    var endDay: MutableLiveData<Long?> = MutableLiveData()

    init {
        calendarCurrentState.also {
            startDay.value = GregorianCalendar(it.get(Calendar.YEAR), it.get(Calendar.MONTH), it.get(Calendar.DAY_OF_MONTH))
                .apply { add(Calendar.MONTH, -1) }.timeInMillis + 32_400_000
            endDay.value = it.timeInMillis
        }
    }
}