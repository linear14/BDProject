package com.bd.bdproject.viewmodel.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.common.timeToString
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightWithTags
import com.bd.bdproject.data.repository.LightRepository
import kotlinx.coroutines.*

class LightViewModel(private val lightRepo: LightRepository): ViewModel() {

    var currentDateCode: String = System.currentTimeMillis().timeToString()

    val lightWithTags: MutableLiveData<LightWithTags> = MutableLiveData()
    var lightBeforeDBAccessed: MutableLiveData<Int?> = MutableLiveData(null)

    suspend fun insertLight(light: Light) {
        lightRepo.insertLight(light)
    }

    fun getLightWithTags() {
        CoroutineScope(Dispatchers.Main).launch {
            val lwt = async(Dispatchers.IO) { lightRepo.selectLightsWithTagsByDateCode(currentDateCode) }
            lightWithTags.value = lwt.await()
        }
    }
}