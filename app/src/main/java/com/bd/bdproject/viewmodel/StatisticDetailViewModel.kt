package com.bd.bdproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightWithTags
import com.bd.bdproject.data.repository.LightRepository
import com.bd.bdproject.data.repository.TagRepository
import com.bd.bdproject.util.timeToLong
import com.bd.bdproject.util.timeToString
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class StatisticDetailViewModel(
    private val lightRepo: LightRepository,
    private val tagRepo: TagRepository
    ): ViewModel() {

    private val _isShowDate = MutableLiveData<Boolean>(false)
    val isShowDate: LiveData<Boolean> = _isShowDate

    val activatedGridPosition = MutableLiveData<Int?>(null)
    val activatedDetailPosition = MutableLiveData<Int?>(null)

    val lights: MutableLiveData<List<Light>> = MutableLiveData()
    val lightWithTags: MutableLiveData<LightWithTags> = MutableLiveData()

    fun getLightsForTag(tagName: String, startDay: Long, endDay: Long) {
        // Log.d("DATE_CODE_TEST", "sd: ${startDay.timeToString()}_${startDay} ____ ed: ${endDay.timeToString()}_${endDay}")
        GlobalScope.launch {
            val result = tagRepo.selectTagWithLightsByTagName(tagName).lights
                .sortedBy { it.dateCode.timeToLong() }
                .filter { it.dateCode.timeToLong() in startDay..endDay }

            /*result.forEach {
                Log.d("DATE_CODE_TEST", "${it.dateCode}_${it.dateCode.timeToLong()}")
            }*/

            lights.postValue(result)
        }
    }

    fun getLightWithTags(dateCode: String): Deferred<LightWithTags> {
        return GlobalScope.async {
            lightRepo.selectLightsWithTagsByDateCode(dateCode)
        }
    }

    fun setShowDate(isShowDate: Boolean) {
        _isShowDate.value = isShowDate
    }

}