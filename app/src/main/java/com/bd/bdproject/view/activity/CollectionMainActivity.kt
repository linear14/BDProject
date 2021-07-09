package com.bd.bdproject.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bd.bdproject.databinding.ActivityCollectionMainBinding
import com.bd.bdproject.dialog.SlideDatePicker
import com.bd.bdproject.common.Constant
import com.bd.bdproject.common.Constant.COLLECTION_MAIN
import com.bd.bdproject.common.Constant.INFO_BRIGHTNESS
import com.bd.bdproject.common.Constant.INFO_DATE_CODE
import com.bd.bdproject.common.timeToLong
import com.bd.bdproject.common.timeToString
import com.bd.bdproject.view.adapter.CollectionCalendarAdapter
import com.bd.bdproject.view.adapter.SpacesItemDecorator
import com.bd.bdproject.viewmodel.CollectionViewModel
import com.bd.bdproject.viewmodel.CheckEnrollStateViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class CollectionMainActivity : AppCompatActivity() {

    lateinit var binding: ActivityCollectionMainBinding
    private val collectionViewModel: CollectionViewModel by viewModel()
    private val checkEnrollStateViewModel: CheckEnrollStateViewModel by viewModel()

    private val calendarAdapter by lazy { CollectionCalendarAdapter { dateCode, brightness ->
        val intent = Intent(this@CollectionMainActivity, DetailActivity::class.java).apply {
            putExtra(INFO_DATE_CODE, dateCode)
            putExtra(INFO_BRIGHTNESS, brightness)
            putExtra(Constant.INFO_SHOULD_HAVE_DRAWER, false)
        }
        detailForActivityResult.launch(intent)
    } }

    private val detailForActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val brightness = result.data?.getIntExtra(INFO_BRIGHTNESS, 0)
            collectionViewModel.todayBrightness = brightness
        }
    }

    private var picker: SlideDatePicker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCollectionMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }

        binding.apply {
            rvCalendar.adapter = calendarAdapter
            rvCalendar.itemAnimator = null
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

    override fun onStop() {
        super.onStop()
        picker?.dismissAllowingStateLoss()
    }

    private fun observeLight() {
        collectionViewModel.lightLiveData.observe(this) {
            calendarAdapter.submitList(it)
        }
    }
    
    private fun setCurrentCalendar() {
        collectionViewModel.apply {
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

        picker = SlideDatePicker(dateBundle) { view, year, monthOfYear, dayOfMonth ->

            val sb = StringBuilder().apply {
                append(year)
                append(if(monthOfYear < 10) "0${monthOfYear}" else monthOfYear)
                append(if(dayOfMonth < 10) "0${dayOfMonth}" else dayOfMonth)
            }

            if(sb.toString().timeToLong() > System.currentTimeMillis()) {
                Snackbar.make(binding.root, "미래의 빛을 등록할 수 없습니다.", Snackbar.LENGTH_SHORT).show()
            } else if(sb.toString() == System.currentTimeMillis().timeToString()) {
                Snackbar.make(binding.root, "오늘의 빛은 홈 화면에서 등록해주세요.", Snackbar.LENGTH_SHORT).show()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val isEnrolledToday = checkEnrollStateViewModel.isEnrolledTodayAsync(sb.toString()).await()
                    if(isEnrolledToday) {
                        Snackbar.make(binding.root, "이미 빛 정보가 등록되어 있는 날입니다.", Snackbar.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(this@CollectionMainActivity, BitdamEnrollActivity::class.java).apply {
                            putExtra(Constant.INFO_PREVIOUS_ACTIVITY, Constant.COLLECTION_MAIN)
                            putExtra("datecode", sb.toString())
                        }

                        startActivity(intent)
                    }
                }
            }
        }
        picker?.show(supportFragmentManager, "SlideDatePicker")
    }

    private fun moveToNextMonth() {
        collectionViewModel.calendarCurrentState.add(Calendar.MONTH, 1)
        setCurrentCalendar()
    }

    private fun moveToPreviousMonth() {
        collectionViewModel.calendarCurrentState.add(Calendar.MONTH, -1)
        setCurrentCalendar()
    }

    override fun onBackPressed() {
        if(collectionViewModel.todayBrightness != null) {
            val resultIntent = Intent().apply {
                putExtra("TYPE", COLLECTION_MAIN)
                putExtra(INFO_BRIGHTNESS, collectionViewModel.todayBrightness)
            }
            setResult(Activity.RESULT_OK, resultIntent)
        }
        super.onBackPressed()
    }
}