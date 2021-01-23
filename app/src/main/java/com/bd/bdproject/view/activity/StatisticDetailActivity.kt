package com.bd.bdproject.view.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.databinding.ActivityStatisticDetailBinding
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.view.adapter.SpacesItemDecorator
import com.bd.bdproject.view.adapter.TagCalendarAdapter
import com.bd.bdproject.viewmodel.StatisticDetailViewModel
import org.koin.android.ext.android.inject
import java.util.*

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
            rvTagCalendar.addItemDecoration(SpacesItemDecorator())
            btnBack.setOnClickListener { onBackPressed() }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.getLightsForTag(
            intent.getStringExtra(INFO_TAG)?:"",
            intent.getLongExtra("START_DAY", System.currentTimeMillis()),
            intent.getLongExtra("END_DAY", System.currentTimeMillis())
        )

        observeLights()
    }

    private fun observeLights() {
        viewModel.lights.observe(this) {
            for(i in it){
                Log.d("LIGHT_TEST", i.dateCode)
            }
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

            val calendarList = it.toMutableList()
            for(i in 0 until headerList.size) {
                calendarList.add(headerList[i].second + i, Light(headerList[i].first, -1, ""))
            }

            calendarAdapter = TagCalendarAdapter(calendarList)

            binding.rvTagCalendar.layoutManager = StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL)
            binding.rvTagCalendar.adapter = calendarAdapter


        }
    }
}