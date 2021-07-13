package com.bd.bdproject.viewmodel

import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.Tags
import com.bd.bdproject.data.repository.LightRepository

class EditViewModel(private val lightRepo: LightRepository): ViewModel() {
    var light: Light? = null
    var tags: Tags? = null

    fun editBrightness(brightness: Int, dateCode: String) {
        lightRepo.updateBrightness(brightness, dateCode)
    }

    fun editMemo(memo: String, dateCode: String) {
        lightRepo.updateMemo(memo, dateCode)
    }
}