package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.util.timeToString

class EnrollViewModel: ViewModel() {
    var dateCode: String? = System.currentTimeMillis().timeToString()

    var brightness: MutableLiveData<Int?> = MutableLiveData()
    var tags: MutableLiveData<List<Tag>?> = MutableLiveData()
    var memo: MutableLiveData<String?> = MutableLiveData()

    var previousActivity: Int? = null
    var previousPage: Int? = null
    var isFragmentTransitionState = false

    fun init() {
        dateCode = System.currentTimeMillis().timeToString()
        brightness = MutableLiveData()
        tags = MutableLiveData()
        memo = MutableLiveData()
    }
}