package com.bd.bdproject.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.BitDamApplication.Companion.applicationContext
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.repository.TagRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TagViewModel(private val tagRepo: TagRepository): ViewModel() {

    val candidateTags = mutableListOf<Tag>()
    val searchedTagNames: MutableLiveData<List<String>> = MutableLiveData()

    fun asyncInsertTag(tags: List<Tag>) {
        GlobalScope.launch { tagRepo.insertTag(tags) }
    }

    fun searchTag(word: String) {
        GlobalScope.launch {
            searchedTagNames.postValue(tagRepo.searchTag("%${word}%"))
        }
    }
}