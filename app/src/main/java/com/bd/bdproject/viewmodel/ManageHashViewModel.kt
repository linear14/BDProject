package com.bd.bdproject.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bd.bdproject.`interface`.OnAsyncWorkFinished
import com.bd.bdproject.data.model.LightTagRelation
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.model.TagWithLights
import com.bd.bdproject.data.repository.LightTagRelationRepository
import com.bd.bdproject.data.repository.TagRepository
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.FILTER_ASC
import com.bd.bdproject.view.activity.ManageHashActivity.Companion.FILTER_DESC
import kotlinx.coroutines.*

class ManageHashViewModel(
    private val tagRepository: TagRepository,
    private val relationRepository: LightTagRelationRepository
    ): ViewModel() {

    val tags: MutableLiveData<List<TagWithLights>> = MutableLiveData()

    val checkedTags: MutableLiveData<MutableSet<TagWithLights>> = MutableLiveData(mutableSetOf())
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
        CoroutineScope(Dispatchers.IO).launch {
            if(word.isNullOrEmpty()) {
                val result = when(filter.value) {
                    FILTER_ASC -> async { tagRepository.selectAllTagsAsc() }
                    FILTER_DESC -> async { tagRepository.selectAllTagsDesc() }
                    else -> return@launch
                }.await()

                launch(Dispatchers.Main) { tags.value = result }
            } else {
                val result = when(filter.value) {
                    FILTER_ASC -> async { tagRepository.searchTagReturnTagAsc("%${word}%") }
                    FILTER_DESC -> async { tagRepository.searchTagReturnTagDesc("%${word}%") }
                    else -> return@launch
                }.await()

                launch(Dispatchers.Main) { tags.value = result }
            }
        }
    }

    suspend fun insertTag(tagName: String) {
        tagRepository.insertTag(listOf(Tag(tagName)))
    }

    suspend fun editTag(oldTag: String, newTag: String) {
        CoroutineScope(Dispatchers.IO).launch {
            tagRepository.updateTag(listOf(oldTag), newTag)
            relationRepository.updateRelations(listOf(oldTag), newTag)
        }
    }

    // 태그 이름 삭제, dateCode와 연결되어 있던 태그들 모두 삭제
    fun deleteTags(tags: List<Tag>) {
        runBlocking {
            val job = CoroutineScope(Dispatchers.IO).launch {
                tagRepository.deleteTag(tags)
                relationRepository.deleteRelationByTag(tags)
            }
            job.join()

            if(job.isCancelled) onAsyncWorkFinished?.onFailure()
            else if(job.isCompleted) onAsyncWorkFinished?.onSuccess()

            searchTag(searchedText.value)
        }
    }

    suspend fun combineTags(combinedTo: String) {
            CoroutineScope(Dispatchers.IO).launch {
                checkedTags.value?.let { tagWithLights ->
                    // 사라질 태그 관련 정보
                    val willBeDeletedList = tagWithLights.toList().map { it.tag }.filter { it.name != combinedTo }
                    tagRepository.deleteTag(willBeDeletedList)

                    // 합쳐질 결과 태그의 사용된 DateCode
                    var alreadyHaveDateCode = mutableListOf<String>()
                    tagWithLights.toList().forEach { twl ->
                        if(twl.tag.name == combinedTo) {
                            alreadyHaveDateCode = twl.lights.map { it.dateCode }.toMutableList()
                            return@forEach
                        }
                    }

                    // 새로운 RelationList
                    val relationList = mutableListOf<LightTagRelation>()
                    tagWithLights.forEach { twl ->
                        twl.lights.forEach { light ->
                            if(light.dateCode !in alreadyHaveDateCode) {
                                relationList.add(LightTagRelation(light.dateCode, combinedTo))
                            }
                        }
                    }

                    // RelationList 로그 함 찍어보자
                    for(i in relationList) {
                        Log.d("RELATION_TEST", "(${i.dateCode}) - ${i.name}")
                    }

                    relationRepository.deleteRelationByTag(willBeDeletedList)
                    relationRepository.insertRelation(relationList)
                }

        }
    }


    // ===========================================================================
    // 상태 보관 작업
    // ===========================================================================

    fun addOrRemoveCheckedTag(tag: TagWithLights) {
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

    fun removeAllCheckedTags() {
        checkedTags.value = mutableSetOf()
    }

    fun setOnAsyncWorkFinishedListener(li: OnAsyncWorkFinished) {
        onAsyncWorkFinished = li
    }

    fun isAlreadyExist(tagName: String): Boolean {
        tags.value?.let { tagList ->
            val nameList = tagList.map { it.tag.name }
            return tagName in nameList
        }
        return true
    }

}