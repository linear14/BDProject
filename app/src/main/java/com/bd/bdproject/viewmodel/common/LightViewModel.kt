package com.bd.bdproject.viewmodel.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.common.timeToString
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightWithTags
import com.bd.bdproject.data.repository.LightRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LightViewModel(private val lightRepo: LightRepository): ViewModel() {

    var currentDateCode: String = System.currentTimeMillis().timeToString()

    val lightWithTags: MutableLiveData<LightWithTags> = MutableLiveData()

    suspend fun insertLight(light: Light) {
        lightRepo.insertLight(light)
    }

    fun getLightWithTags() {
        GlobalScope.launch {
            lightWithTags.postValue(lightRepo.selectLightsWithTagsByDateCode(currentDateCode))
        }
    }

    fun editBrightness(brightness: Int, dateCode: String) {
        lightRepo.updateBrightness(brightness, dateCode)
    }

    fun editMemo(memo: String?, dateCode: String) {
        GlobalScope.launch {
            lightRepo.updateMemo(memo, dateCode)
        }
    }
}