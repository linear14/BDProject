package com.bd.bdproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.`interface`.OnAsyncWorkFinished
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.repository.LightTagRelationRepository
import com.bd.bdproject.data.repository.TagRepository
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.FILTER_ASC
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.FILTER_DESC
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ManageHashViewModel(
    val tagRepository: TagRepository,
    val relationRepository: LightTagRelationRepository
    ): ViewModel() {

    val tags: MutableLiveData<List<Tag>> = MutableLiveData()

    val checkedTags: MutableLiveData<MutableSet<Tag>> = MutableLiveData(mutableSetOf())
    val searchedText: MutableLiveData<String?> = MutableLiveData(null)
    val filter: MutableLiveData<Int> = MutableLiveData(FILTER_ASC)

    private var onAsyncWorkFinished: OnAsyncWorkFinished? = null

    init {
        searchTag(searchedText.value)
    }

    // ===========================================================================
    // Room DB 연결 작업
    // ===========================================================================


    fun searchTag(word: String?) {
        when(filter.value) {
            FILTER_ASC -> {
                if(word.isNullOrEmpty()) {
                    GlobalScope.launch {
                        tags.postValue(tagRepository.selectAllTagsAsc())
                    }
                } else {
                    GlobalScope.launch {
                        tags.postValue(tagRepository.searchTagReturnTagAsc("%${word}%"))
                    }
                }
            }

            FILTER_DESC -> {
                if(word.isNullOrEmpty()) {
                    GlobalScope.launch {
                        tags.postValue(tagRepository.selectAllTagsDesc())
                    }
                } else {
                    GlobalScope.launch {
                        tags.postValue(tagRepository.searchTagReturnTagDesc("%${word}%"))
                    }
                }
            }
        }
    }

    // 태그 이름 삭제, dateCode와 연결되어 있던 태그들 모두 삭제
    fun deleteTags(tags: List<Tag>) {
        runBlocking {
            val job = GlobalScope.launch {
                tagRepository.deleteTag(tags)
                relationRepository.deleteRelationByTag(tags)
            }

            job.join()
            if(job.isCancelled) onAsyncWorkFinished?.onFailure()
            else if(job.isCompleted) onAsyncWorkFinished?.onSuccess()

            searchTag(searchedText.value)
        }

    }


    // ===========================================================================
    // 상태 보관 작업
    // ===========================================================================

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

    fun setOnAsyncWorkFinishedListener(li: OnAsyncWorkFinished) {
        onAsyncWorkFinished = li
    }

}