package com.bd.bdproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightWithTags
import com.bd.bdproject.data.repository.LightRepository
import com.bd.bdproject.data.repository.TagRepository
import com.bd.bdproject.common.timeToLong
import com.bd.bdproject.data.model.TagCalendar
import kotlinx.coroutines.*

class StatisticDetailViewModel(
    private val lightRepo: LightRepository,
    private val tagRepo: TagRepository
    ): ViewModel() {

    private val _isShowDate = MutableLiveData<Boolean>(false)
    val isShowDate: LiveData<Boolean> = _isShowDate

    var activatedGridPosition: Int? = null
    var activatedDetailPosition: Int? = null

    val lights: MutableLiveData<List<Light>> = MutableLiveData()
    val calendarList: MutableLiveData<MutableList<TagCalendar>> = MutableLiveData()

    var tagName: String? = null
    var startDay: Long? = null
    var endDay: Long? = null

    fun lateInitData() {
        if(tagName == null || startDay == null || endDay == null) {
            return
        }
        getLightsForTag(tagName!!, startDay!!, endDay!!)
    }

    fun isLoaded(): Boolean = !calendarList.value.isNullOrEmpty()

    private fun getLightsForTag(tagName: String, startDay: Long, endDay: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = async(Dispatchers.IO) {
                tagRepo.selectTagWithLightsByTagName(tagName).lights
                    .sortedBy { it.dateCode.timeToLong() }
                    .filter { it.dateCode.timeToLong() in startDay..endDay }
            }

            lights.value = result.await()
        }
    }

    fun getLightWithTags(dateCode: String): Deferred<LightWithTags> {
        return CoroutineScope(Dispatchers.IO).async {
            lightRepo.selectLightsWithTagsByDateCode(dateCode)
        }
    }

    fun setShowDate(isShowDate: Boolean) {
        _isShowDate.value = isShowDate
    }

}