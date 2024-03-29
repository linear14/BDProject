package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.common.timeToLong
import com.bd.bdproject.common.timeToString
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.repository.LightRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class CollectionViewModel(private val lightRepo: LightRepository) : ViewModel() {

    val lightLiveData: MutableLiveData<List<Light>> = MutableLiveData()

    val calendarCurrentState = GregorianCalendar()
    var todayBrightness: Int? = null

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

        return dateCodeList
    }

    fun getLightsForMonth(dateCodes: MutableList<String>) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = async(Dispatchers.IO) {
                lightRepo.selectLightForSpecificDays(dateCodes)
            }.await().sortedBy { it.dateCode.timeToLong() }

            lightLiveData.value = result
        }
    }
}