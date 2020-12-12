package com.bd.bdproject.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.bd.bdproject.BitDamApplication.Companion.applicationContext
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.repository.TagRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TagViewModel(private val tagRepo: TagRepository): ViewModel() {

    fun asyncInsertTag(tags: List<Tag>) {
        runBlocking {
            val job = GlobalScope.launch { tagRepo.insertTag(tags) }
            job.join()
            Toast.makeText(
                applicationContext(),
                "${tags.size}개의 태그 등록 완료",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}