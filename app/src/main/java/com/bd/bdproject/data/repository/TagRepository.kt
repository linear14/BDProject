package com.bd.bdproject.data.repository

import androidx.room.Query
import androidx.room.Transaction
import com.bd.bdproject.data.dao.TagDao
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.model.TagWithLights

class TagRepository(private val tagDao: TagDao) {

    /*fun selectTagWithLights(): LiveData<List<TagWithLights>> {
        return tagDao.selectTagWithLights()
    }*/

    // CREATE
    suspend fun insertTag(tags: List<Tag>) {
        tagDao.insertTag(tags)
    }

    // READ
    fun searchTag(word: String): List<String> {
        return tagDao.searchTag(word)
    }

    fun searchTagOrderByUsedCount(word: String): List<String> {
        return tagDao.searchTagOrderByUsedCount(word)
    }

    fun selectTagWithLightsByTagName(tagName: String): TagWithLights {
        return tagDao.selectTagWithLightsByTagName(tagName)
    }

    /*suspend fun deleteTag(tags: List<Tag>) {
        tagDao.deleteTag(tags)
    }*/
}