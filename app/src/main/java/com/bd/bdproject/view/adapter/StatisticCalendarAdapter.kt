package com.bd.bdproject.view.adapter

import android.graphics.Color
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
import com.bd.bdproject.common.timeToString
import com.bd.bdproject.viewmodel.StatisticCalendarViewModel
import java.util.*

class StatisticCalendarAdapter(private val calendarList: MutableList<StatisticCalendar>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_STAT_HEADER = 1
        const val VIEW_TYPE_STAT_EMPTY = 2
        const val VIEW_TYPE_STAT_DAY = 3
    }

    private var viewModel: StatisticCalendarViewModel? = null
    private val calendar = Calendar.getInstance()

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
            calendar.timeInMillis = dateCode

            binding.apply {
                val dateCodeString = dateCode.timeToString()
                tvDate.text = dateCodeString.substring(6, 8)
                when(calendar.get(Calendar.DAY_OF_WEEK)) {
                    1 -> tvDate.setTextColor(Color.parseColor("#FF0000"))
                    7 -> tvDate.setTextColor(Color.parseColor("#008EFF"))
                    else -> tvDate.setTextColor(Color.parseColor("#000000"))
                }

                viewModel?.let { vm ->
                    val startDay = vm.duration.value?.first
                    val endDay = vm.duration.value?.second

                    // 하나만 찍혀있는 경우
                    if(endDay == null) {
                        // 시작 일
                        if(dateCode == startDay) {
                            lineLeft.visibility = View.INVISIBLE
                            lineRight.visibility = View.INVISIBLE
                            circleMarker.visibility = View.VISIBLE
                        } else {
                            lineLeft.visibility = View.INVISIBLE
                            lineRight.visibility = View.INVISIBLE
                            circleMarker.visibility = View.GONE
                        }
                    } else {
                        /*
                            순서대로
                            1. 두 개 모두 같은 날짜 찍혀있는 경우에는 한 날짜에만 원이 생기도록
                            2. 시작 날과 종료 일 사이에는 선만 보이도록
                            3. 시작 날짜일 경우에는 원과 오른쪽 선만 보이도록
                            4. 종료 날짜일 경우에는 원과 왼쪽 선만 보이도록
                            5. 선택 날짜에 포함되지 않는 경우에는 아무것도 보이지 않도록
                         */
                        if(startDay != null) {
                            if(startDay == endDay && dateCode == startDay) {
                                lineLeft.visibility = View.INVISIBLE
                                lineRight.visibility = View.INVISIBLE
                                circleMarker.visibility = View.VISIBLE
                            } else if(dateCode in startDay + 1 until endDay) {
                                lineLeft.visibility = View.VISIBLE
                                lineRight.visibility = View.VISIBLE
                                circleMarker.visibility = View.GONE
                            } else if(dateCode == startDay) {
                                lineLeft.visibility = View.INVISIBLE
                                lineRight.visibility = View.VISIBLE
                                circleMarker.visibility = View.VISIBLE
                            } else if(dateCode == endDay) {
                                lineLeft.visibility = View.VISIBLE
                                lineRight.visibility = View.INVISIBLE
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
                                if(dateCode <= duration.first!!) {
                                    vm.duration.value = Pair(dateCode, null)
                                } else {
                                    val tempStart = duration.first
                                    vm.duration.value = Pair(tempStart, dateCode)
                                }
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
