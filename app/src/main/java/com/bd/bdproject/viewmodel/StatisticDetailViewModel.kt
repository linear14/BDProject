package com.bd.bdproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.repository.TagRepository
import com.bd.bdproject.util.timeToLong
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StatisticDetailViewModel(private val tagRepo: TagRepository): ViewModel() {

    private val _isShowDate = MutableLiveData<Boolean>(false)
    val isShowDate: LiveData<Boolean> = _isShowDate

    val lights: MutableLiveData<List<Light>> = MutableLiveData()

    fun getLightsForTag(tagName: String, startDay: Long, endDay: Long) {
        GlobalScope.launch {
            val result = tagRepo.selectTagWithLightsByTagName(tagName).lights
                .sortedBy { it.dateCode.timeToLong() }
                .filter { it.dateCode.timeToLong() in startDay..endDay }

            lights.postValue(result)
        }
    }

    fun setShowDate(isShowDate: Boolean) {
        _isShowDate.value = isShowDate
    }

}