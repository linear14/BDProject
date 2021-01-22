package com.bd.bdproject.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.databinding.ActivityStatisticDetailBinding
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.view.adapter.TagCalendarAdapter
import com.bd.bdproject.viewmodel.StatisticDetailViewModel
import org.koin.android.ext.android.inject

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
            calendarAdapter = TagCalendarAdapter(it.toMutableList())

            binding.rvTagCalendar.layoutManager = StaggeredGridLayoutManager(7, StaggeredGridLayoutManager.VERTICAL)
            binding.rvTagCalendar.adapter = calendarAdapter
        }
    }
}