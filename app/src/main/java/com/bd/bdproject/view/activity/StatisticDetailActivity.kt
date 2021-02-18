package com.bd.bdproject.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.TagViewType
import com.bd.bdproject.`interface`.OnCalendarItemClickedListener
import com.bd.bdproject.data.model.TagCalendar
import com.bd.bdproject.databinding.ActivityStatisticDetailBinding
import com.bd.bdproject.util.Constant.INFO_TAG
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.util.withDateSeparator
import com.bd.bdproject.view.adapter.SpacesItemDecorator
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
            tvDuration.text =
                "${intent.getLongExtra("START_DAY", System.currentTimeMillis()).withDateSeparator(".")} - ${intent.getLongExtra("END_DAY", System.currentTimeMillis()).withDateSeparator(".")}"
            rvTagCalendar.addItemDecoration(SpacesItemDecorator())
            btnBack.setOnClickListener { onBackPressed() }
        }

        observeLights()
    }

    override fun onResume() {
        super.onResume()

        binding.lifecycleOwner = this

        viewModel.getLightsForTag(
            intent.getStringExtra(INFO_TAG)?:"",
            intent.getLongExtra("START_DAY", System.currentTimeMillis()),
            intent.getLongExtra("END_DAY", System.currentTimeMillis())
        )


        setSwitchDateVisibility()
    }

    private fun observeLights() {
        viewModel.lights.observe(this) {
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

            calendarAdapter = TagCalendarAdapter(calendarList, viewModel, object: OnCalendarItemClickedListener {
                override fun onGridClicked(dateCode: String, wantedPosition: Int) {
                    var newPosition = wantedPosition

                    viewModel.activatedDetailPosition.value?.let { oldPosition ->
                        calendarList.removeAt(oldPosition)
                        if(oldPosition < wantedPosition) {
                            newPosition = wantedPosition - 1
                        }
                        //calendarAdapter?.notifyItemRemoved(oldPosition)
                        calendarAdapter?.notifyDataSetChanged()
                    }

                    viewModel.activatedDetailPosition.value = newPosition
                    calendarList.add(newPosition, TagCalendar(TagViewType.CALENDAR_DETAIL, date = dateCode))
                    //calendarAdapter?.notifyItemInserted(newPosition)
                    calendarAdapter?.notifyDataSetChanged()
                }

                override fun onDetailClosed(position: Int) {
                    viewModel.activatedDetailPosition.value = null
                    calendarList.removeAt(position)
                    //calendarAdapter?.notifyItemRemoved(position)
                    calendarAdapter?.notifyDataSetChanged()
                }
            })

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