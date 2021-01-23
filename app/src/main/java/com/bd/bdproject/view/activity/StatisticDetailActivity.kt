package com.bd.bdproject.view.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.ViewType
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.TagCalendar
import com.bd.bdproject.databinding.ActivityStatisticDetailBinding
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.view.adapter.SpacesItemDecorator
import com.bd.bdproject.view.adapter.TagCalendarAdapter
import com.bd.bdproject.viewmodel.StatisticDetailViewModel
import org.koin.android.ext.android.inject
import org.koin.core.qualifier._q

class StatisticDetailActivity : AppCompatActivity() {

    lateinit var binding: ActivityStatisticDetailBinding

    private val viewModel: StatisticDetailViewModel by inject()

    private var calendarAdapter: TagCalendarAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticDetailBinding.inflate(layoutInflater).apply {
            setContentView(root)
        }
        binding.apply {
            tvTitle.text = "# ${intent.getStringExtra(INFO_TAG)?:"null"}"
            tvStartDay.text = "${intent.getLongExtra("START_DAY", System.currentTimeMillis()).timeToString()}"
            tvEndDay.text = "${intent.getLongExtra("END_DAY", System.currentTimeMillis()).timeToString()}"
            rvTagCalendar.addItemDecoration(SpacesItemDecorator())
            btnBack.setOnClickListener { onBackPressed() }
        }
    }

    override fun onResume() {
        super.onResume()

        binding.lifecycleOwner = this

        viewModel.getLightsForTag(
            intent.getStringExtra(INFO_TAG)?:"",
            intent.getLongExtra("START_DAY", System.currentTimeMillis()),
            intent.getLongExtra("END_DAY", System.currentTimeMillis())
        )

        observeLights()

        setSwitchDateVisibility()
    }

    private fun observeLights() {
        viewModel.lights.observe(this) {
            /*for(i in it){
                Log.d("LIGHT_TEST", i.dateCode)
            }*/
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
                TagCalendar(ViewType.CALENDAR_GRID, light = light)
            }.toMutableList()

            for(i in 0 until headerList.size) {
                calendarList.add(
                    headerList[i].second + i,
                    TagCalendar(ViewType.CALENDAR_HEADER, date = headerList[i].first)
                )
            }

            calendarAdapter = TagCalendarAdapter(calendarList, viewModel) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }

            binding.rvTagCalendar.layoutManager = StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL)
            binding.rvTagCalendar.adapter = calendarAdapter
        }
    }

    private fun setSwitchDateVisibility() {
        binding.switchDateVisibility.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                viewModel.setShowDate(true)
            } else {
                viewModel.setShowDate(false)
            }
            binding.rvTagCalendar.adapter?.notifyDataSetChanged()
        }
    }
}