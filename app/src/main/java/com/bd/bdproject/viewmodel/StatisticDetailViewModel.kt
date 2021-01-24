package com.bd.bdproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightWithTags
import com.bd.bdproject.data.repository.LightRepository
import com.bd.bdproject.data.repository.TagRepository
import com.bd.bdproject.util.timeToLong
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

    val isActivatedDetailPosition = MutableLiveData<Int?>(null)

    val lights: MutableLiveData<List<Light>> = MutableLiveData()
    val lightWithTags: MutableLiveData<LightWithTags> = MutableLiveData()

    fun getLightsForTag(tagName: String, startDay: Long, endDay: Long) {
        GlobalScope.launch {
            val result = tagRepo.selectTagWithLightsByTagName(tagName).lights
                .sortedBy { it.dateCode.timeToLong() }
                .filter { it.dateCode.timeToLong() in startDay..endDay }

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