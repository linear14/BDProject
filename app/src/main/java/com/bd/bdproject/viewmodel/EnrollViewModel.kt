package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.util.timeToString

class EnrollViewModel: ViewModel() {
    val dateCode: MutableLiveData<String> = MutableLiveData(System.currentTimeMillis().timeToString())

    var brightness: MutableLiveData<Int?> = MutableLiveData()
    var tags: MutableLiveData<List<Tag>?> = MutableLiveData()
    var memo: MutableLiveData<String?> = MutableLiveData()

    val previousActivity: MutableLiveData<Int?> = MutableLiveData()
    val previousPage: MutableLiveData<Int?> = MutableLiveData()

    fun init() {
        dateCode.value = System.currentTimeMillis().timeToString()
        brightness = MutableLiveData()
        tags = MutableLiveData()
        memo = MutableLiveData()
    }
}