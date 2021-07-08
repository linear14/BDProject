package com.bd.bdproject.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.TagViewType
import com.bd.bdproject.`interface`.OnCalendarItemClickedListener
import com.bd.bdproject.data.model.TagCalendar
import com.bd.bdproject.databinding.ActivityStatisticDetailBinding
import com.bd.bdproject.common.BitDamApplication
import com.bd.bdproject.common.Constant.INFO_TAG
import com.bd.bdproject.common.withDateSeparator
import com.bd.bdproject.view.adapter.SpacesItemDecorator
import com.bd.bdproject.view.adapter.TagCalendarAdapter
import com.bd.bdproject.viewmodel.StatisticDetailViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class StatisticDetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityStatisticDetailBinding
    private val viewModel: StatisticDetailViewModel by viewModel()
    private val calendarAdapter: TagCalendarAdapter by lazy {
        TagCalendarAdapter(
            viewModel = viewModel,
            onCalendarItemClickedListener = object : OnCalendarItemClickedListener {
                override fun onGridClicked(dateCode: String, position: Int) {
                    val tempCalendarList = viewModel.calendarList.value ?: mutableListOf()
                    val oldDetailPosition = viewModel.activatedDetailPosition
                    var newDetailPosition = position

                    if (oldDetailPosition != null) {
                        tempCalendarList.removeAt(oldDetailPosition)
                        if (oldDetailPosition < newDetailPosition) {
                            newDetailPosition = position - 1
                        }
                    }

                    viewModel.activatedDetailPosition = newDetailPosition
                    tempCalendarList.add(
                        newDetailPosition,
                        TagCalendar(TagViewType.CALENDAR_DETAIL, date = dateCode)
                    )
                    viewModel.calendarList.value = tempCalendarList
                }

                override fun onDetailClosed(position: Int) {
                    val tempCalendarList = viewModel.calendarList.value ?: mutableListOf()
                    if (!tempCalendarList.isNullOrEmpty()) {
                        tempCalendarList.removeAt(position)
                    }
                    viewModel.activatedDetailPosition = null
                    viewModel.calendarList.value = tempCalendarList
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticDetailBinding.inflate(layoutInflater).apply {
            setContentView(root)

            viewModel.tagName = intent.getStringExtra(INFO_TAG)?:""
            viewModel.startDay = intent.getLongExtra("START_DAY", System.currentTimeMillis())
            viewModel.endDay = intent.getLongExtra("END_DAY", System.currentTimeMillis())
            viewModel.lateInitData()

            tvTitle.text = "# ${viewModel.tagName}"
            tvDuration.text = "${viewModel.startDay.withDateSeparator(".")} - ${viewModel.endDay.withDateSeparator(".")}"
            switchDateVisibility.isChecked = BitDamApplication.pref.isShowDate
            viewModel.setShowDate(switchDateVisibility.isChecked)
            rvTagCalendar.layoutManager = StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL)
            rvTagCalendar.adapter = calendarAdapter
            rvTagCalendar.addItemDecoration(SpacesItemDecorator())
            btnBack.setOnClickListener { onBackPressed() }
        }
        setSwitchDateVisibility()
        observeLights()
        observeCalendarList()
    }

    private fun observeLights() {
        viewModel.lights.observe(this) {
            if(!viewModel.isLoaded()) {
                val headerList = mutableListOf<Pair<String, Int>>()
                var monthYear = "-1"

                for((i, light) in it.withIndex()) {
                    if(light.dateCode.startsWith(monthYear)) {
                        continue
                    } else {
                        monthYear = light.dateCode.substring(0, 6)
                        headerList.add(Pair(monthYear, i))
                    }
                }

                val calendarList = it.map { light ->
                    TagCalendar(TagViewType.CALENDAR_GRID, light = light)
                }.toMutableList()

                for(i in 0 until headerList.size) {
                    calendarList.add(
                        headerList[i].second + i,
                        TagCalendar(TagViewType.CALENDAR_HEADER, date = headerList[i].first)
                    )
                }

                viewModel.calendarList.value = calendarList
            }
        }
    }

    private fun observeCalendarList() {
        viewModel.calendarList.observe(this) { calendarList ->
            calendarAdapter.calendarList = calendarList
            calendarAdapter.notifyDataSetChanged()
        }
    }

    private fun setSwitchDateVisibility() {
        binding.switchDateVisibility.setOnCheckedChangeListener { _, isChecked ->
            BitDamApplication.pref.isShowDate = isChecked
            if(isChecked) {
                viewModel.setShowDate(true)
            } else {
                viewModel.setShowDate(false)
            }
            binding.rvTagCalendar.adapter?.notifyDataSetChanged()
        }
    }
}