package com.bd.bdproject.data.repository

import com.bd.bdproject.data.dao.TagDao
import com.bd.bdproject.data.model.SearchedTag
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
    fun selectAllTagsAsc(): List<TagWithLights> {
        return tagDao.selectAllTagsAsc()
    }

    fun selectAllTagsDesc(): List<TagWithLights> {
        return tagDao.selectAllTagsDesc()
    }

    fun searchTag(word: String): List<String> {
        return tagDao.searchTag(word)
    }

    fun searchTagReturnTagAsc(word: String): List<TagWithLights> {
        return tagDao.searchTagReturnTagAsc(word)
    }

    fun searchTagReturnTagDesc(word: String): List<TagWithLights> {
        return tagDao.searchTagReturnTagDesc(word)
    }

    fun searchTagOrderByUsedCount(word: String): List<SearchedTag> {
        return tagDao.searchTagOrderByUsedCount(word)
    }

    fun selectTagWithLightsByTagName(tagName: String): TagWithLights {
        return tagDao.selectTagWithLightsByTagName(tagName)
    }

    // UPDATE
    suspend fun updateTag(oldTag: List<String>, newTag: String) {
        tagDao.updateTag(oldTag, newTag)
    }

    // DELETE
    suspend fun deleteTag(tags: List<Tag>) {
        tagDao.deleteTag(tags.map { it.name })
    }
}