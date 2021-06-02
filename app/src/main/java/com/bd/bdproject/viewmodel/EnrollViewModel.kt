package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.util.timeToString

class EnrollViewModel: ViewModel() {
    var dateCode: String = System.currentTimeMillis().timeToString()

    var brightness: Int = 0
    var tags: List<Tag> = mutableListOf()
    var memo: String? = null

    var previousActivity: Int? = null
    var previousPage: Int? = null
    var isFragmentTransitionState = false

    fun init() {
        dateCode = System.currentTimeMillis().timeToString()
        brightness = 0
        tags = mutableListOf()
        memo = null
    }
}