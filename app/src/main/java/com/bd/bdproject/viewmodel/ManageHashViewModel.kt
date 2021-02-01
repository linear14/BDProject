package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.repository.TagRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ManageHashViewModel(val tagRepository: TagRepository): ViewModel() {

    val tags: MutableLiveData<List<Tag>> = MutableLiveData()

    val checkedTags: MutableLiveData<MutableSet<Tag>> = MutableLiveData(mutableSetOf())

    init {
        getAllTags(TYPE_ASC)
    }

    fun getAllTags(type: String) {
        when(type) {
            TYPE_ASC -> {
                GlobalScope.launch {
                    tags.postValue(tagRepository.selectAllTagsAsc())
                }
            }

            TYPE_DESC -> {
                GlobalScope.launch {
                    tags.postValue(tagRepository.selectAllTagsDesc())
                }
            }
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


    companion object {
        const val TYPE_ASC = "TYPE_ASC"
        const val TYPE_DESC = "TYPE_DESC"
    }
}