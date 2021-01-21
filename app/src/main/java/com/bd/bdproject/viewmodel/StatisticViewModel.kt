package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightWithTags
import com.bd.bdproject.data.model.MyHashTag
import com.bd.bdproject.data.repository.LightRepository
import com.bd.bdproject.util.timeToString
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
            startDay.value = GregorianCalendar(it.get(Calendar.YEAR), it.get(Calendar.MONTH), it.get(Calendar.DAY_OF_MONTH))
                .apply { add(Calendar.MONTH, -1) }.timeInMillis + 32_400_000
            endDay.value = it.timeInMillis
        }
    }

    private fun getDateCode(): MutableList<String> {
        val dateCodeList = mutableListOf<String>()

        // TODO 최초 처음 지정 날짜 STRING CODE가 들어오지 않음
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
    fun makeTagStatistic(list: List<LightWithTags>): MutableList<MyHashTag> {
        val tags: MutableList<MyHashTag> = mutableListOf()
        val tempMap: HashMap<String, Pair<Int, Int>> = hashMapOf()
        
        for(lightWithTags in list) {
            for(tag in lightWithTags.tags) {
                val tempSum = tempMap[tag.name]?.first?:0
                val tempCnt = tempMap[tag.name]?.second?:0

                tempMap[tag.name] = Pair(tempSum + lightWithTags.light.bright, tempCnt + 1)
            }
        }

        for(i in tempMap) {
            tags.add(MyHashTag(i.key, (i.value.first / i.value.second), i.value.second))
        }

        return tags
    }
}