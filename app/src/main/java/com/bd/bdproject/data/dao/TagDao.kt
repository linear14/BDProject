package com.bd.bdproject.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.model.TagWithLights

@Dao
interface TagDao {

    /*@Transaction
    @Query("SELECT * FROM tag")
    fun selectTagWithLights(): LiveData<List<TagWithLights>>*/

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tags: List<Tag>)

    /*@Query("UPDATE tag SET name=:name WHERE name=:name")
    suspend fun updateTag(name: String)

    @Delete
    suspend fun deleteTag(tags: List<Tag>)*/
}