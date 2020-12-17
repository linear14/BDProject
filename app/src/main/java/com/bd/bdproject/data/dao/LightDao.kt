package com.bd.bdproject.data.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

}