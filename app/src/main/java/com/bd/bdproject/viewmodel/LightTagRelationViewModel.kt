package com.bd.bdproject.viewmodel

import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.LightTagRelation
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.repository.LightTagRelationRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LightTagRelationViewModel(private val lightTagRelationRepository: LightTagRelationRepository) : ViewModel() {

    fun insertRelation(dateCode: String, tagList: List<Tag>) {
        GlobalScope.launch {
            val relationList = mutableListOf<LightTagRelation>()
            for(i in tagList) {
                relationList.add(LightTagRelation(dateCode, i.name))
            }
            lightTagRelationRepository.insertRelation(relationList)
        }
    }
}