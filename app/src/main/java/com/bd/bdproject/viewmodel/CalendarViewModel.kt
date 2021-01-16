package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.repository.LightRepository
import com.bd.bdproject.util.timeToLong
import com.bd.bdproject.util.timeToString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class CalendarViewModel(private val lightRepo: LightRepository) : ViewModel() {

    val lightLiveData: MutableLiveData<List<Light>> = MutableLiveData()

    val calendarCurrentState = GregorianCalendar()

    fun getDateCode(year: Int, month: Int): MutableList<String> {

        val dateCodeList = mutableListOf<String>()

        val calendarStart = GregorianCalendar(year, month, 1)
        val calendarEnd = if(month != 11) {
            GregorianCalendar(year, month + 1, 1).apply { add(Calendar.DATE, -1) }
        } else {
            GregorianCalendar(year + 1, 0, 1).apply { add(Calendar.DATE, -1) }
        }


        while(!calendarStart.after(calendarEnd)) {
            dateCodeList.add(calendarStart.timeInMillis.timeToString())
            calendarStart.add(Calendar.DATE, 1)
        }

        /*Log.d("DATE_CODE_TEST", "*********start**********")
        for(i in dateCodeList) {
            Log.d("DATE_CODE_TEST", i)
        }
        Log.d("DATE_CODE_TEST", "*********end**********")*/

        return dateCodeList
    }

    fun getLightsForMonth(dateCodes: MutableList<String>) {
        GlobalScope.launch {
            val list = lightRepo.selectLightForSpecificDays(dateCodes)
            list.sortedBy { it.dateCode.timeToLong() }

            lightLiveData.postValue(list)
        }
    }
}