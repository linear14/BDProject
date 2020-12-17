package com.bd.bdproject.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bd.bdproject.data.dao.LightDao
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightWithTags

class LightRepository(private val lightDao: LightDao) {

    // CREATE
    suspend fun insertLight(light: Light) {
        lightDao.insertLight(light)
    }

    // READ
    fun isEnrolledToday(dateCode: String): Boolean {
        return lightDao.isEnrolledToday(dateCode)
    }

    fun selectLightsWithTagsByDateCode(dateCode: String): LightWithTags {
        return lightDao.selectLightsWithTagsByDateCode(dateCode)
    }
}