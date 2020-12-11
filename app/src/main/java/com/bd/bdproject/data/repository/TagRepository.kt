package com.bd.bdproject.data.repository

import androidx.lifecycle.LiveData
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

    /*suspend fun deleteTag(tags: List<Tag>) {
        tagDao.deleteTag(tags)
    }*/
}