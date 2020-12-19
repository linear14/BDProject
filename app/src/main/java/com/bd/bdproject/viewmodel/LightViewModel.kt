package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightWithTags
import com.bd.bdproject.data.repository.LightRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LightViewModel(private val lightRepo: LightRepository): ViewModel() {

    val lightWithTags: MutableLiveData<LightWithTags> = MutableLiveData()

    fun asyncInsertLight(light: Light) {
        GlobalScope.launch { lightRepo.insertLight(light) }
    }

    fun getLightWithTags(dateCode: String) {
        GlobalScope.launch {
            lightWithTags.postValue(lightRepo.selectLightsWithTagsByDateCode(dateCode))
        }
    }
}