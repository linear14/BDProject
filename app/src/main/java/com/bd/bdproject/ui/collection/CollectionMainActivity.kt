package com.bd.bdproject.ui.collection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.bd.bdproject.databinding.ActivityCollectionMainBinding
import com.bd.bdproject.ui.collection.adapter.CalendarAdapter
import com.bd.bdproject.viewmodel.collection.CalendarViewModel
import org.koin.android.ext.android.inject

class CollectionMainActivity : AppCompatActivity() {

    lateinit var binding: ActivityCollectionMainBinding
    private val calendarViewModel: CalendarViewModel by inject()

    private val calendarAdapter by lazy { CalendarAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCollectionMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            rvCalendar.adapter = calendarAdapter
        }

        calendarViewModel.getLightsForMonth(calendarViewModel.getDateCode(2020, 12))
        observeLight()
    }

    private fun observeLight() {
        calendarViewModel.lightLiveData.observe(this) {
            calendarAdapter.submitList(it)
        }
    }
}