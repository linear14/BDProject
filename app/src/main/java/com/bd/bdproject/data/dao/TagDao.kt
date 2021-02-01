package com.bd.bdproject.data.dao

import androidx.room.*
import com.bd.bdproject.data.model.LightWithTags
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

    // READ
    @Query("SELECT * FROM tag ORDER BY name ASC")
    fun selectAllTagsAsc(): List<Tag>

    @Query("SELECT * FROM tag ORDER BY name DESC")
    fun selectAllTagsDesc(): List<Tag>

    @Query("SELECT name FROM tag WHERE name LIKE :word")
    fun searchTag(word: String): List<String>

    @Query("SELECT A.name " +
            "FROM tag A, lightTagRelation B " +
            "WHERE A.name LIKE :word " +
            "AND A.name = B.name " +
            "GROUP BY A.name " +
            "ORDER BY Count(A.name) DESC " +
            "LIMIT 3")
    fun searchTagOrderByUsedCount(word: String): List<String>

    @Transaction
    @Query("SELECT * FROM tag WHERE name = :tagName")
    fun selectTagWithLightsByTagName(tagName: String): TagWithLights

    /*@Query("UPDATE tag SET name=:name WHERE name=:name")
    suspend fun updateTag(name: String)

    @Delete
    suspend fun deleteTag(tags: List<Tag>)*/
}