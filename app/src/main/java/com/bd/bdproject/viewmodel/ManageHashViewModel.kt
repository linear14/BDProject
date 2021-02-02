package com.bd.bdproject.viewmodel

import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.repository.TagRepository
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.FILTER_ASC
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.FILTER_DESC
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ManageHashViewModel(val tagRepository: TagRepository): ViewModel() {

    val tags: MutableLiveData<List<Tag>> = MutableLiveData()

    val checkedTags: MutableLiveData<MutableSet<Tag>> = MutableLiveData(mutableSetOf())

    init {
        getAllTags(FILTER_ASC)
    }

    fun getAllTags(type: Int) {
        when(type) {
            FILTER_ASC -> {
                GlobalScope.launch {
                    tags.postValue(tagRepository.selectAllTagsAsc())
                }
            }

            FILTER_DESC -> {
                GlobalScope.launch {
                    tags.postValue(tagRepository.selectAllTagsDesc())
                }
            }
        }
    }

    fun searchTag(word: String) {
        GlobalScope.launch {
            tags.postValue(tagRepository.searchTagReturnTag("%${word}%"))
        }
    }

    fun addOrRemoveCheckedTag(tag: Tag) {
        val temp = checkedTags.value
        temp?.let {
            if(tag in temp) {
                // 있으니깐 제거
                temp.remove(tag)
            } else {
                // 없으니깐 추가
                temp.add(tag)
            }
            checkedTags.value = temp
        }
    }

    fun addAllCheckedTags() {
        // TODO 화면에 보이는 모든 태그 set에 넣기 구현
    }

    fun removeAllCheckedTags() {
        checkedTags.value = mutableSetOf()
    }

}