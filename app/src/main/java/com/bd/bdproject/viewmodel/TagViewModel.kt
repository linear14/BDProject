package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.repository.TagRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TagViewModel(private val tagRepo: TagRepository): ViewModel() {

    val candidateTags: MutableLiveData<MutableList<Tag>> = MutableLiveData()
    val searchedTagNames: MutableLiveData<List<String>> = MutableLiveData()

    init {
        candidateTags.value = mutableListOf()
    }

    fun asyncInsertTag(tags: List<Tag>?) {
        tags?.let {
            GlobalScope.launch { tagRepo.insertTag(tags) }
        }
    }

    fun searchTag(word: String?) {
        GlobalScope.launch {
            if(word == null) {
                searchedTagNames.postValue(listOf())
            } else {
                searchedTagNames.postValue(tagRepo.searchTag("%${word}%"))
            }
        }
    }

    fun insertTagToCandidate(tagName: String) {
        val temp = candidateTags.value
        temp?.add(Tag(tagName))

        candidateTags.value = temp
    }
}