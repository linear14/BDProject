package com.bd.bdproject.data.dao

import androidx.room.*
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.LightWithTags

@Dao
interface LightDao {

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLight(light: Light)

    // READ
    @Query("SELECT EXISTS(SELECT 1 FROM light WHERE dateCode = :dateCode)")
    fun isEnrolledToday(dateCode: String): Boolean

    @Transaction
    @Query("SELECT * FROM light WHERE dateCode = :dateCode")
    fun selectLightsWithTagsByDateCode(dateCode: String): LightWithTags

    @Query("SELECT * FROM light WHERE dateCode IN (:dateCodes)")
    fun selectLightForSpecificDays(dateCodes: List<String>): List<Light>

    @Transaction
    @Query("SELECT * FROM light WHERE dateCode IN (:dateCodes)")
    fun selectLightWithTagsForSpecificDays(dateCodes: List<String>): List<LightWithTags>

    // UPDATE
    @Query("UPDATE light SET bright = :brightness WHERE dateCode = :dateCode")
    fun updateBrightness(brightness: Int, dateCode: String)

    @Query("UPDATE light SET memo = :memo WHERE dateCode = :dateCode")
    fun updateMemo(memo: String, dateCode: String)
}