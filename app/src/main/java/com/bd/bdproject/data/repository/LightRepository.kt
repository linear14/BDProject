package com.bd.bdproject.data.repository

import androidx.lifecycle.LiveData
import com.bd.bdproject.data.dao.LightDao
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightWithTags

class LightRepository(private val lightDao: LightDao) {

    /*fun selectAll(): LiveData<List<Light>> {
        return lightDao.selectAll()
    }

    fun selectLightsWithTags(): LiveData<List<LightWithTags>> {
        return lightDao.selectLightsWithTags()
    }*/

    // CREATE
    suspend fun insertLight(light: Light) {
        lightDao.insertLight(light)
    }

    // READ
    fun isEnrolledToday(dateCode: String): Boolean {
        return lightDao.isEnrolledToday(dateCode)
    }

    /*suspend fun updateLight(dateCode: Int, newBright: Int, newMemo: String) {
        lightDao.updateLight(dateCode, newBright, newMemo)
    }

    suspend fun deleteLight(light: Light) {
        lightDao.deleteLight(light)
    }*/
}