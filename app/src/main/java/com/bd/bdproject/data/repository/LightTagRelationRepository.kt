package com.bd.bdproject.data.repository

import com.bd.bdproject.data.dao.LightTagRelationDao
import com.bd.bdproject.data.model.LightTagRelation
import com.bd.bdproject.data.model.Tag

class LightTagRelationRepository(private val lightTagCrossRefDao: LightTagRelationDao) {

    // CREATE
    fun insertRelation(relation: List<LightTagRelation>) {
        lightTagCrossRefDao.insertRelation(relation)
    }

    // UPDATE
    suspend fun updateRelations(oldTag: String, newTag: String) {
        lightTagCrossRefDao.updateRelations(oldTag, newTag)
    }

    // DELETE
    suspend fun deleteRelationsAll(dateCode: String) {
        lightTagCrossRefDao.deleteRelationsAll(dateCode)
    }

    suspend fun deleteRelationByTag(tags: List<Tag>) {
        lightTagCrossRefDao.deleteRelationByTag(tags.map { it.name })
    }

    /*suspend fun deleteRelation(relation: List<LightTagRelation>) {
        lightTagCrossRefDao.deleteRelation(relation)
    }

    suspend fun deleteRelationByDateCode(dateCode: Int) {
        lightTagCrossRefDao.deleteRelationByDateCode(dateCode)
    }

    suspend fun deleteRelationByTagName(name: List<String>) {
        lightTagCrossRefDao.deleteRelationByTagName(name)
    }*/
}