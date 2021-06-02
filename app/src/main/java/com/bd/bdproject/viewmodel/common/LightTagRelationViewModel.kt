package com.bd.bdproject.viewmodel.common

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.LightTagRelation
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.repository.LightTagRelationRepository
import com.bd.bdproject.util.BitDamApplication.Companion.applicationContext
import kotlinx.coroutines.*

class LightTagRelationViewModel(private val lightTagRelationRepository: LightTagRelationRepository) : ViewModel() {

    fun insertRelation(dateCode: String, tagList: List<Tag>?) {
        tagList?.let {
            val relationList = mutableListOf<LightTagRelation>()
            for (i in tagList) {
                relationList.add(LightTagRelation(dateCode, i.name))
            }
            lightTagRelationRepository.insertRelation(relationList)

        } ?: Toast.makeText(applicationContext(), "태그 리스트 오류 발생", Toast.LENGTH_SHORT).show()
    }

    fun updateRelationsAll(dateCode: String, tagList: List<Tag>?) {
        CoroutineScope(Dispatchers.IO).launch {
            val job = launch { lightTagRelationRepository.deleteRelationsAll(dateCode) }
            job.join()
            insertRelation(dateCode, tagList)
        }
    }
}