package com.bd.bdproject.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.databinding.ActivityCollectionMainBinding
import com.bd.bdproject.dialog.SlideDatePicker
import com.bd.bdproject.util.Constant
import com.bd.bdproject.util.Constant.INFO_DATE_CODE
import com.bd.bdproject.util.timeToLong
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.view.adapter.CollectionCalendarAdapter
import com.bd.bdproject.view.adapter.SpacesItemDecorator
import com.bd.bdproject.viewmodel.CalendarViewModel
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.*

class CollectionMainActivity : AppCompatActivity() {

    lateinit var binding: ActivityCollectionMainBinding
    private val calendarViewModel: CalendarViewModel by inject()
    private val checkEnrollStateViewModel: CheckEnrollStateViewModel by inject()

    private val calendarAdapter by lazy { CollectionCalendarAdapter { dateCode ->
        startActivity(Intent(this@CollectionMainActivity, DetailActivity::class.java).apply {
            putExtra(INFO_DATE_CODE, dateCode)
            putExtra(Constant.INFO_SHOULD_HAVE_DRAWER, false)
        })
    } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCollectionMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        binding.apply {
            rvCalendar.adapter = calendarAdapter
            rvCalendar.addItemDecoration(SpacesItemDecorator())

            btnPreviousMonth.setOnClickListener { moveToPreviousMonth() }
            btnNextMonth.setOnClickListener { moveToNextMonth() }
            btnAdd.setOnClickListener { addNewLight() }
            btnBack.setOnClickListener { onBackPressed() }
        }

        observeLight()
    }

    override fun onResume() {
        super.onResume()

        setCurrentCalendar()
    }

    private fun observeLight() {
        calendarViewModel.lightLiveData.observe(this) {
            calendarAdapter.submitList(it)
        }
    }
    
    private fun setCurrentCalendar() {
        calendarViewModel.apply {
            binding.tvCurrentYear.text = "${calendarCurrentState.get(Calendar.YEAR)}"
            binding.tvCurrentMonth.text = "${calendarCurrentState.get(Calendar.MONTH) + 1}월"
            getLightsForMonth(getDateCode(calendarCurrentState.get(Calendar.YEAR), calendarCurrentState.get(Calendar.MONTH)))
        }
    }

    private fun addNewLight() {
        val dateBundle = Bundle().apply {
            val dateCode = System.currentTimeMillis().timeToString()
            dateCode.let {
                putInt("YEAR", it.substring(0, 4).toInt())
                putInt("MONTH", it.substring(4, 6).toInt())
                putInt("DATE", it.substring(6, 8).toInt())
            }
        }

        val picker = SlideDatePicker(dateBundle) { view, year, monthOfYear, dayOfMonth ->

            val sb = StringBuilder().apply {
                append(year)
                append(if(monthOfYear < 10) "0${monthOfYear}" else monthOfYear)
                append(if(dayOfMonth < 10) "0${dayOfMonth}" else dayOfMonth)
            }

            if(sb.toString().timeToLong() > System.currentTimeMillis()) {
                Toast.makeText(this, "미래의 빛을 등록할 수 없습니다.\n다른 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else if(sb.toString() == System.currentTimeMillis().timeToString()) {
                Toast.makeText(this, "오늘의 빛은 홈 화면에서 등록해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                GlobalScope.launch {
                    val deferred = checkEnrollStateViewModel.isEnrolledTodayAsync(sb.toString())
                    deferred.join()

                    if(deferred.getCompleted()) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CollectionMainActivity, "이미 빛 정보가 등록되어 있는 날입니다.\n다른 날짜를 선택해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            val intent = Intent(this@CollectionMainActivity, BitdamEnrollActivity::class.java).apply {
                                putExtra(Constant.INFO_PREVIOUS_ACTIVITY, Constant.COLLECTION_MAIN)
                                putExtra("datecode", sb.toString())
                            }

                            startActivity(intent)
                        }
                    }
                }
            }
        }
        picker.show(supportFragmentManager, "SlideDatePicker")
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