package com.bd.bdproject.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bd.bdproject.data.model.LightTagRelation

@Dao
interface LightTagRelationDao {

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRelation(relation: List<LightTagRelation>)

    // DELETE
    @Query("DELETE FROM LightTagRelation WHERE dateCode=:dateCode")
    suspend fun deleteRelationsAll(dateCode: String)

    /*// TODO 세 가지 방법중 어떤 방식으로 사용할건지 확실히 정하는게 좋을듯
    @Delete
    suspend fun deleteRelation(relation: List<LightTagRelation>)

    @Query("DELETE FROM LightTagRelation WHERE dateCode=:dateCode")
    suspend fun deleteRelationByDateCode(dateCode: Int)

    @Query("DELETE FROM LightTagRelation WHERE name in (:name)")
    suspend fun deleteRelationByTagName(name: List<String>)*/
}