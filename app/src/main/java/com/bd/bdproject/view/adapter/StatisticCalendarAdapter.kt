package com.bd.bdproject.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.StatisticViewType
import com.bd.bdproject.data.model.StatisticCalendar
import com.bd.bdproject.databinding.ItemStatisticCalendarDayBinding
import com.bd.bdproject.databinding.ItemStatisticCalendarEmptyBinding
import com.bd.bdproject.databinding.ItemStatisticCalendarHeaderBinding
import com.bd.bdproject.util.timeToString

class StatisticCalendarAdapter(private val calendarList: MutableList<StatisticCalendar>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_STAT_HEADER = 1
        const val VIEW_TYPE_STAT_EMPTY = 2
        const val VIEW_TYPE_STAT_DAY = 3
    }

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
            }
        }

    }

    // ViewHolder End
}
