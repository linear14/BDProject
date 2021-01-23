package com.bd.bdproject.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.databinding.ItemTagCalendarGridBinding
import com.bd.bdproject.databinding.ItemTagCalendarHeaderBinding
import com.bd.bdproject.viewmodel.StatisticDetailViewModel

/***
 *  @parms [lights]
 *  기간동안의 빛이 들어있다.
 *  내부의 dateCode를 분석했을 때,
 *  202011 형태의 6자리일 경우 Header 표시로,
 *  20201119 형태의 8자리일 경우 Grid를 채우는 ViewType을 가지게 된다.
 */
class TagCalendarAdapter(private val lights: MutableList<Light>, val viewModel: StatisticDetailViewModel) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TAG_CALENDAR_HEADER = 1
        const val VIEW_TAG_CALENDAR_GRID = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (lights[position].dateCode.length) {
            8 -> VIEW_TAG_CALENDAR_GRID
            else -> VIEW_TAG_CALENDAR_HEADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TAG_CALENDAR_HEADER -> {
                val binding = ItemTagCalendarHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                val params = (binding.root.layoutParams as StaggeredGridLayoutManager.LayoutParams).apply {
                    isFullSpan = true
                }

                binding.root.layoutParams = params
                HeaderViewHolder(binding)
            }
            else -> {
                GridViewHolder(
                    ItemTagCalendarGridBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            VIEW_TAG_CALENDAR_HEADER -> {
                (holder as HeaderViewHolder).onBind(lights[position].dateCode)
            }
            else -> {
                (holder as GridViewHolder).onBind(lights[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return lights.size
    }

    inner class HeaderViewHolder(val binding: ItemTagCalendarHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: String) {
            binding.apply {
                tvYearMonth.text = "${item.substring(0, 4)}년 ${item.substring(4, 6)}월"
            }
        }
    }

    inner class GridViewHolder(val binding: ItemTagCalendarGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: Light) {
            binding.apply {
                light = item
                vm = viewModel
                executePendingBindings()
            }
        }

    }
}