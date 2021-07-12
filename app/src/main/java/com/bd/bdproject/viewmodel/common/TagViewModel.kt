package com.bd.bdproject.viewmodel.common

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.common.returnBoundaryList
import com.bd.bdproject.data.model.SearchedTag
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.repository.TagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TagViewModel(private val tagRepo: TagRepository): ViewModel() {

    val candidateTags: MutableLiveData<MutableList<Tag>> = MutableLiveData()
    val searchedTagNames: MutableLiveData<List<SearchedTag>> = MutableLiveData()

    init {
        searchTag(null)
    }

    suspend fun insertTag(tags: List<Tag>?) {
        tagRepo.insertTag(tags?: mutableListOf())
    }

    fun searchTag(word: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            if(word == null) {
                launch(Dispatchers.Main) { searchedTagNames.value = listOf() }
            } else {
                val def1 = async { tagRepo.searchTagOrderByUsedCount("%${word}%") }
                val def2 = async { tagRepo.searchTag("%${word}%") }

                val countList = def1.await().toMutableList()
                val tagNameList = countList.map { it.name }
                def2.await().forEach {
                    if(it in tagNameList) {
                        countList[tagNameList.indexOf(it)].count++
                    } else {
                        countList.add(SearchedTag(it, 1))
                    }
                }
                countList.sortByDescending { it.count }
                launch(Dispatchers.Main) { searchedTagNames.value = countList.returnBoundaryList(3) }
            }
        }
    }

    fun insertTagToCandidate(tagName: String) {
        val temp = candidateTags.value?: mutableListOf()
        temp.add(Tag(tagName))

        candidateTags.value = temp
    }

    fun editTagCandidate(oldTagName: String, newTagName: String) {
        val temp = candidateTags.value?: mutableListOf()

        for((i, tag) in temp.withIndex()) {
            if(tag.name == oldTagName) {
                temp[i] = Tag(newTagName)
            }
        }

        candidateTags.value = temp
    }

    fun deleteTagCandidate(tagName: String) {
        val temp = candidateTags.value?: mutableListOf()
        temp.remove(Tag(tagName))
        candidateTags.value = temp
    }
}