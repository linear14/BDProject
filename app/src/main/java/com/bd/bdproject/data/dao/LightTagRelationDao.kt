package com.bd.bdproject.data.dao

import androidx.room.*
import com.bd.bdproject.data.model.LightTagRelation
import com.bd.bdproject.data.model.Tag

@Dao
interface LightTagRelationDao {

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRelation(relation: List<LightTagRelation>)

    // UPDATE
    @Query("UPDATE LightTagRelation SET name=:newTag WHERE name in (:oldTag)")
    suspend fun updateRelations(oldTag: List<String>, newTag: String)

    // DELETE
    @Query("DELETE FROM LightTagRelation WHERE dateCode=:dateCode")
    suspend fun deleteRelationsAll(dateCode: String)

    @Query("DELETE FROM LightTagRelation WHERE name in (:tags)")
    suspend fun deleteRelationByTag(tags: List<String>)

    /*// TODO 세 가지 방법중 어떤 방식으로 사용할건지 확실히 정하는게 좋을듯
    @Delete
    suspend fun deleteRelation(relation: List<LightTagRelation>)

    @Query("DELETE FROM LightTagRelation WHERE dateCode=:dateCode")
    suspend fun deleteRelationByDateCode(dateCode: Int)

    @Query("DELETE FROM LightTagRelation WHERE name in (:name)")
    suspend fun deleteRelationByTagName(name: List<String>)*/
}