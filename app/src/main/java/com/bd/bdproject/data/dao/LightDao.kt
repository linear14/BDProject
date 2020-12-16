package com.bd.bdproject.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bd.bdproject.data.model.Light

@Dao
interface LightDao {

/*    @Query("SELECT * FROM light")
    fun selectAll(): LiveData<List<Light>>

    @Transaction
    @Query("SELECT * FROM light")
    fun selectLightsWithTags(): LiveData<List<LightWithTags>>*/

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLight(light: Light)

    // READ
    @Query("SELECT EXISTS(SELECT 1 FROM light WHERE dateCode = :dateCode)")
    fun isEnrolledToday(dateCode: String): Boolean

    /*@Query("UPDATE light SET bright=:newBright, memo=:newMemo WHERE dateCode=:dateCode")
    suspend fun updateLight(dateCode: Int, newBright: Int, newMemo: String)

    @Delete
    suspend fun deleteLight(light: Light)*/
}