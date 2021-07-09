package com.bd.bdproject.viewmodel

import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.repository.LightRepository
import kotlinx.coroutines.*

class CheckEnrollStateViewModel(private val lightRepo: LightRepository): ViewModel() {

    var isVisitedSetting = false

    fun isEnrolledTodayAsync(dateCode: String) =
        CoroutineScope(Dispatchers.IO).async {
            lightRepo.isEnrolledToday(dateCode)
        }

    fun getTodayBrightness(dateCode: String) =
        CoroutineScope(Dispatchers.IO).async {
            val lwt = async { lightRepo.selectLightsWithTagsByDateCode(dateCode) }.await()
            lwt.light.bright
        }
}