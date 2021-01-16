package com.bd.bdproject.viewmodel

import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.repository.LightRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class SplashViewModel(private val lightRepo: LightRepository): ViewModel() {

    fun isEnrolledTodayAsync(dateCode: String): Deferred<Boolean> {
        return GlobalScope.async { lightRepo.isEnrolledToday(dateCode) }
    }
}