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
    fun selectAllTagsAsc(): List<Tag> {
        return tagDao.selectAllTagsAsc()
    }

    fun selectAllTagsDesc(): List<Tag> {
        return tagDao.selectAllTagsDesc()
    }

    fun searchTag(word: String): List<String> {
        return tagDao.searchTag(word)
    }

    fun searchTagReturnTagAsc(word: String): List<Tag> {
        return tagDao.searchTagReturnTagAsc(word)
    }

    fun searchTagReturnTagDesc(word: String): List<Tag> {
        return tagDao.searchTagReturnTagDesc(word)
    }

    fun searchTagOrderByUsedCount(word: String): List<String> {
        return tagDao.searchTagOrderByUsedCount(word)
    }

    fun selectTagWithLightsByTagName(tagName: String): TagWithLights {
        return tagDao.selectTagWithLightsByTagName(tagName)
    }

    // DELETE
    suspend fun deleteTag(tags: List<Tag>) {
        tagDao.deleteTag(tags.map { it.name })
    }
}