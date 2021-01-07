package com.bd.bdproject.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.bd.bdproject.BitDamApplication.Companion.applicationContext
import com.bd.bdproject.data.model.LightTagRelation
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.repository.LightTagRelationRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
        GlobalScope.launch {
            lightTagRelationRepository.deleteRelationsAll(dateCode)
            insertRelation(dateCode, tagList)
        }
    }
}