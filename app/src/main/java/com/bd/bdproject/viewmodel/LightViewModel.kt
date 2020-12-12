package com.bd.bdproject.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.bd.bdproject.BitDamApplication.Companion.applicationContext
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.repository.LightRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LightViewModel(private val lightRepo: LightRepository): ViewModel() {

    fun asyncInsertLight(light: Light) {
        runBlocking {
            val job = GlobalScope.launch { lightRepo.insertLight(light) }
            job.join()
            Toast.makeText(
                applicationContext(),
                "${light.dateCode}번의 빛 등록 완료",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}