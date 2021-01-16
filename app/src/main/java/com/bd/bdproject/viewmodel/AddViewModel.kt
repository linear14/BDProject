package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Tag

class AddViewModel: ViewModel() {
    val brightness: MutableLiveData<Int?> = MutableLiveData()
    val tags: MutableLiveData<List<Tag>?> = MutableLiveData()
    val memo: MutableLiveData<String?> = MutableLiveData()

    val previousPage: MutableLiveData<Int?> = MutableLiveData()
}