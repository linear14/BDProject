package com.bd.bdproject.data.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bd.bdproject.data.model.Tag

@Dao
interface TagDao {

    /*@Transaction
    @Query("SELECT * FROM tag")
    fun selectTagWithLights(): LiveData<List<TagWithLights>>*/

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tags: List<Tag>)

    // READ
    @Query("SELECT name FROM tag WHERE name LIKE :word")
    fun searchTag(word: String): List<String>

    /*@Query("UPDATE tag SET name=:name WHERE name=:name")
    suspend fun updateTag(name: String)

    @Delete
    suspend fun deleteTag(tags: List<Tag>)*/
}