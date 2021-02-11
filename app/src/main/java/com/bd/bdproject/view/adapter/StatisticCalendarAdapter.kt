package com.bd.bdproject.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.StatisticViewType
import com.bd.bdproject.data.model.StatisticCalendar
import com.bd.bdproject.databinding.ItemStatisticCalendarDayBinding
import com.bd.bdproject.databinding.ItemStatisticCalendarEmptyBinding
import com.bd.bdproject.databinding.ItemStatisticCalendarHeaderBinding
import com.bd.bdproject.util.timeToString
import com.bd.bdproject.viewmodel.StatisticCalendarViewModel

class StatisticCalendarAdapter(private val calendarList: MutableList<StatisticCalendar>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_STAT_HEADER = 1
        const val VIEW_TYPE_STAT_EMPTY = 2
        const val VIEW_TYPE_STAT_DAY = 3
    }

    private var viewModel: StatisticCalendarViewModel? = null

    override fun getItemViewType(position: Int): Int {
        return when(calendarList[position].type) {
            StatisticViewType.CALENDAR_HEADER -> VIEW_TYPE_STAT_HEADER
            StatisticViewType.CALENDAR_EMPTY -> VIEW_TYPE_STAT_EMPTY
            StatisticViewType.CALENDAR_DAY -> VIEW_TYPE_STAT_DAY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            VIEW_TYPE_STAT_HEADER -> {
                val binding = ItemStatisticCalendarHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                val param = binding.root.layoutParams as StaggeredGridLayoutManager.LayoutParams
                param.isFullSpan = true

                binding.root.layoutParams = param

                HeaderViewHolder(binding)
            }

            VIEW_TYPE_STAT_EMPTY -> {
                val binding = ItemStatisticCalendarEmptyBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                EmptyViewHolder(binding)
            }

             else -> {
                 val binding = ItemStatisticCalendarDayBinding.inflate(
                     LayoutInflater.from(parent.context),
                     parent,
                     false
                 )

                 DayViewHolder(binding)
             }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            VIEW_TYPE_STAT_HEADER -> {
                (holder as HeaderViewHolder).onBind(calendarList[position].dateLong!!)
            }
            VIEW_TYPE_STAT_EMPTY -> {
                (holder as EmptyViewHolder).onBind()
            }
            else -> {
                (holder as DayViewHolder).onBind(calendarList[position].dateLong!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return calendarList.size
    }

    // ViewHolder Start

    inner class HeaderViewHolder(val binding: ItemStatisticCalendarHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun onBind(dateCode: Long) {
                binding.apply {
                    val dateCodeString = dateCode.timeToString()
                    tvHeader.text = "${dateCodeString.substring(0, 4)}년 ${dateCodeString.substring(4, 6)}월"
                }
            }

    }

    inner class EmptyViewHolder(val binding: ItemStatisticCalendarEmptyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind() {

        }

    }

    inner class DayViewHolder(val binding: ItemStatisticCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(dateCode: Long) {
            binding.apply {
                val dateCodeString = dateCode.timeToString()
                tvDate.text = dateCodeString.substring(6, 8)

                viewModel?.let { vm ->
                    val startDay = vm.duration.value?.first
                    val endDay = vm.duration.value?.second

                    if(endDay == null) {
                        if(dateCode == startDay) {
                            lineLeft.visibility = View.INVISIBLE
                            lineRight.visibility = View.INVISIBLE
                            circleMarker.visibility = View.VISIBLE
                        }
                    } else {
                        if(startDay != null) {
                            if(startDay == endDay && dateCode == startDay) {
                                lineLeft.visibility = View.INVISIBLE
                                lineRight.visibility = View.INVISIBLE
                                circleMarker.visibility = View.VISIBLE
                            } else if(dateCode in startDay + 1 until endDay) {
                                lineLeft.visibility = View.VISIBLE
                                lineRight.visibility = View.VISIBLE
                            } else if(dateCode == startDay) {
                                lineRight.visibility = View.VISIBLE
                                circleMarker.visibility = View.VISIBLE
                            } else if(dateCode == endDay) {
                                lineLeft.visibility = View.VISIBLE
                                circleMarker.visibility = View.VISIBLE
                            } else {
                                lineLeft.visibility = View.INVISIBLE
                                lineRight.visibility = View.INVISIBLE
                                circleMarker.visibility = View.GONE
                            }
                        }
                    }

                }


                // 시작, 끝 날짜 지정 (viewmodel에 담습니다.)
                root.setOnClickListener {
                    viewModel?.let { vm ->
                        vm.duration.value?.let { duration ->
                            if(duration.first != null && duration.second != null) {
                                vm.duration.value = Pair(dateCode, null)
                            } else {
                                val tempStart = duration.first
                                vm.duration.value = Pair(tempStart, dateCode)
                            }
                        }

                        notifyDataSetChanged()

                    }
                }
            }
        }

    }

    // ViewHolder End

    fun setViewModel(vm: StatisticCalendarViewModel) {
        this.viewModel = vm
    }
}