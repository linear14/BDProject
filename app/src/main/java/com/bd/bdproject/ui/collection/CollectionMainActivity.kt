package com.bd.bdproject.ui.collection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.bd.bdproject.databinding.ActivityCollectionMainBinding
import com.bd.bdproject.ui.collection.adapter.CalendarAdapter
import com.bd.bdproject.viewmodel.collection.CalendarViewModel
import org.koin.android.ext.android.inject
import java.util.*

class CollectionMainActivity : AppCompatActivity() {

    lateinit var binding: ActivityCollectionMainBinding
    private val calendarViewModel: CalendarViewModel by inject()

    private val calendarAdapter by lazy { CalendarAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCollectionMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        binding.apply {
            setCurrentCalendar()
            rvCalendar.adapter = calendarAdapter

            btnPreviousMonth.setOnClickListener { moveToPreviousMonth() }
            btnNextMonth.setOnClickListener { moveToNextMonth() }
        }

        observeLight()
    }

    private fun observeLight() {
        calendarViewModel.lightLiveData.observe(this) {
            calendarAdapter.submitList(it)
        }
    }
    
    private fun setCurrentCalendar() {
        calendarViewModel.apply {
            binding.tvCurrentCalendar.text = "${calendarCurrentState.get(Calendar.YEAR)}년 ${calendarCurrentState.get(Calendar.MONTH) + 1}월"
            getLightsForMonth(getDateCode(calendarCurrentState.get(Calendar.YEAR), calendarCurrentState.get(Calendar.MONTH)))
        }
    }

    private fun moveToNextMonth() {
        calendarViewModel.calendarCurrentState.add(Calendar.MONTH, 1)
        setCurrentCalendar()
    }

    private fun moveToPreviousMonth() {
        calendarViewModel.calendarCurrentState.add(Calendar.MONTH, -1)
        setCurrentCalendar()
    }
    
                    
}