package com.bd.bdproject.data.repository

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

    fun selectLightForSpecificDays(dateCodes: List<String>): List<Light> {
        return lightDao.selectLightForSpecificDays(dateCodes)
    }

    // UPDATE
    fun updateBrightness(brightness: Int, dateCode: String) {
        lightDao.updateBrightness(brightness, dateCode)
    }

    fun updateMemo(memo: String?, dateCode: String) {
        lightDao.updateMemo(memo?:"", dateCode)
    }
}