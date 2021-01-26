package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.LightWithTags
import com.bd.bdproject.data.model.StatisticTagResult
import com.bd.bdproject.data.repository.LightRepository
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.util.toLightLabel
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class StatisticViewModel(val lightRepo: LightRepository): ViewModel() {

    val calendarCurrentState = GregorianCalendar()

    var startDay: MutableLiveData<Long?> = MutableLiveData()
    var endDay: MutableLiveData<Long?> = MutableLiveData()

    val lightForDuration: MutableLiveData<List<LightWithTags>> = MutableLiveData()

    init {
        calendarCurrentState.also {
            startDay.value = it.timeInMillis - (30L * 86400L * 1000L)
            endDay.value = it.timeInMillis

            // Log.d("DATE_CODE_TEST", "sd: ${startDay.value} ____ ed: ${endDay.value}")
        }
    }

    private fun getDateCode(): MutableList<String> {
        val dateCodeList = mutableListOf<String>()

        val calendarStart = GregorianCalendar.getInstance().apply {
            timeInMillis = startDay.value?:System.currentTimeMillis()
        }

        val calendarEnd = GregorianCalendar.getInstance().apply {
            timeInMillis = endDay.value?:System.currentTimeMillis()
        }

        while(!calendarStart.after(calendarEnd)) {
            dateCodeList.add(calendarStart.timeInMillis.timeToString())
            calendarStart.add(Calendar.DATE, 1)
        }

        return dateCodeList
    }

    fun getLightWithTagsForDuration() {
        GlobalScope.launch {
            val list = lightRepo.selectLightWithTagsForSpecificDays(getDateCode())
            lightForDuration.postValue(list)
        }
    }

    // todo 값이 일정하지가 않음. 확인하기
    fun makeTagStatistic(list: List<LightWithTags>): MutableList<StatisticTagResult> {
        val tags: MutableList<StatisticTagResult> = mutableListOf()
        val tempMap: HashMap<String, Pair<Int, Int>> = hashMapOf()
        
        for(lightWithTags in list) {
            for(tag in lightWithTags.tags) {
                val tempSum = tempMap[tag.name]?.first?:0
                val tempCnt = tempMap[tag.name]?.second?:0

                tempMap[tag.name] = Pair(tempSum + lightWithTags.light.bright, tempCnt + 1)
            }
        }

        for(i in tempMap) {
            tags.add(StatisticTagResult(i.key, (i.value.first / i.value.second), i.value.second))
        }

        return tags
    }

    fun makePieChartEntry(list: List<LightWithTags>): MutableList<PieEntry> {
        val pieEntry = mutableListOf<PieEntry>()
        val tempMap: HashMap<Int, Int> = hashMapOf()

        for(lwt in list) {
            val key = when(lwt.light.bright) {
                in 0..20 -> 0
                in 21..40 -> 1
                in 41..60 -> 2
                in 61..80 -> 3
                in 81..100 -> 4
                else -> -1
            }

            if(tempMap.containsKey(key)) {
                val previousValue = tempMap[key]?:0
                tempMap[key] = previousValue + 1
            } else {
                tempMap[key] = 1
            }
        }


        pieEntry.apply {
            for(i in -1..4) {
                addPieEntry(tempMap, pieEntry, i)
            }
        }

        return pieEntry
    }

    private fun addPieEntry(map: HashMap<Int, Int>, entry: MutableList<PieEntry>, i: Int) {
        if(map.containsKey(i)) {
            entry.add(PieEntry(map[i]?.toFloat()?:0f, i.toLightLabel()))
        }
    }
}