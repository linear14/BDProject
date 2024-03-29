package com.bd.bdproject.data.dao

import androidx.room.*
import com.bd.bdproject.data.model.LightWithTags
import com.bd.bdproject.data.model.SearchedTag
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
    @Transaction
    @Query("SELECT * FROM tag ORDER BY name ASC")
    fun selectAllTagsAsc(): List<TagWithLights>

    @Transaction
    @Query("SELECT * FROM tag ORDER BY name DESC")
    fun selectAllTagsDesc(): List<TagWithLights>

    @Query("SELECT name FROM tag WHERE name LIKE :word")
    fun searchTag(word: String): List<String>

    @Transaction
    @Query("SELECT * FROM tag WHERE name LIKE :word ORDER BY name ASC")
    fun searchTagReturnTagAsc(word: String): List<TagWithLights>

    @Transaction
    @Query("SELECT * FROM tag WHERE name LIKE :word ORDER BY name DESC")
    fun searchTagReturnTagDesc(word: String): List<TagWithLights>

    @Query("SELECT A.name, Count(A.name) as count " +
            "FROM tag A, lightTagRelation B " +
            "WHERE A.name LIKE :word " +
            "AND A.name = B.name " +
            "GROUP BY A.name " +
            "ORDER BY Count(A.name) DESC " +
            "LIMIT 3")
    fun searchTagOrderByUsedCount(word: String): List<SearchedTag>

    @Transaction
    @Query("SELECT * FROM tag WHERE name = :tagName")
    fun selectTagWithLightsByTagName(tagName: String): TagWithLights

    // UPDATE
    @Query("UPDATE tag SET name=:newTag WHERE name in (:oldTag)")
    suspend fun updateTag(oldTag: List<String>, newTag: String)

    // DELETE
    @Query("DELETE FROM tag WHERE name in (:tags)")
    suspend fun deleteTag(tags: List<String>)

    /*@Query("UPDATE tag SET name=:name WHERE name=:name")
    suspend fun updateTag(name: String)

    )*/
}