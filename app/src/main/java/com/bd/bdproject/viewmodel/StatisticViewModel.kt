package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.BitdamLog
import com.bd.bdproject.common.timeToString
import com.bd.bdproject.data.model.LightWithTags
import com.bd.bdproject.data.model.StatisticTagResult
import com.bd.bdproject.data.repository.LightRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class StatisticViewModel(val lightRepo: LightRepository): ViewModel() {

    private val calendarCurrentState = GregorianCalendar().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    var duration: MutableLiveData<Pair<Long, Long>> = MutableLiveData()
    val lightForDuration: MutableLiveData<List<LightWithTags>> = MutableLiveData()

    init {
        calendarCurrentState.also {
            val startDay = it.timeInMillis - (30L * 86400L * 1000L)
            val endDay = it.timeInMillis

            BitdamLog.titleLogger("StatisticViewModel에 처음 들어가는 날짜정보 (init)")
            BitdamLog.dateCodeLogger(startDay)
            BitdamLog.dateCodeLogger(endDay)

            // Gregorian Calendar test
            val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss:SSS")
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = startDay
            BitdamLog.contentLogger(sdf.format(calendar.time))

            calendar.timeInMillis = endDay
            BitdamLog.contentLogger(sdf.format(calendar.time))

            duration.value = Pair(startDay, endDay)
        }
    }

    private fun getDateCode(): MutableList<String> {
        val dateCodeList = mutableListOf<String>()

        val calendarStart = GregorianCalendar.getInstance().apply {
            timeInMillis = duration.value?.first ?: System.currentTimeMillis()
        }

        val calendarEnd = GregorianCalendar.getInstance().apply {
            timeInMillis = duration.value?.second ?: System.currentTimeMillis()
        }

        while(!calendarStart.after(calendarEnd)) {
            dateCodeList.add(calendarStart.timeInMillis.timeToString())
            calendarStart.add(Calendar.DATE, 1)
        }

        return dateCodeList
    }

    fun getLightWithTagsForDuration() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = async(Dispatchers.IO) { lightRepo.selectLightWithTagsForSpecificDays(getDateCode()) }.await()
            lightForDuration.value = result
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

    fun makePieChartEntry(list: List<LightWithTags>): MutableList<Pair<Int, Int>> {
        val data = mutableListOf<Pair<Int, Int>>()
        val tempMap: HashMap<Int, Int> = hashMapOf()

        for(lwt in list) {
            val key = when(lwt.light.bright) {
                in 0..20 -> 1
                in 21..40 -> 2
                in 41..60 -> 3
                in 61..80 -> 4
                in 81..100 -> 5
                else -> 0
            }

            if(tempMap.containsKey(key)) {
                val previousValue = tempMap[key]?:0
                tempMap[key] = previousValue + 1
            } else {
                tempMap[key] = 1
            }
        }

        for(i in 1..5) {
            if(tempMap.containsKey(i)) {
                data.add(Pair(i, tempMap[i]?:0))
            }
        }

        for(i in data) {
            BitdamLog.contentLogger("단계: ${i.first}, 갯수: ${i.second}")
        }

        return data
    }
}