package com.bd.bdproject.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.ViewType
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.TagCalendar
import com.bd.bdproject.databinding.ItemTagCalendarDetailBinding
import com.bd.bdproject.databinding.ItemTagCalendarGridBinding
import com.bd.bdproject.databinding.ItemTagCalendarHeaderBinding
import com.bd.bdproject.viewmodel.StatisticDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TagCalendarAdapter(
    private var calendarList: MutableList<TagCalendar>,
    val viewModel: StatisticDetailViewModel,
    val onGridClicked: (dateCode: String, position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TAG_CALENDAR_HEADER = 1
        const val VIEW_TAG_CALENDAR_GRID = 2
        const val VIEW_TAG_DETAIL = 3
    }

    // Adapter Config Start

    override fun getItemViewType(position: Int): Int {
        return when (calendarList[position].viewType) {
            ViewType.CALENDAR_HEADER -> VIEW_TAG_CALENDAR_HEADER
            ViewType.CALENDAR_GRID -> VIEW_TAG_CALENDAR_GRID
            ViewType.CALENDAR_DETAIL -> VIEW_TAG_DETAIL
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
            VIEW_TAG_CALENDAR_GRID -> {
                GridViewHolder(
                    ItemTagCalendarGridBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                val binding = ItemTagCalendarDetailBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                val params = (binding.root.layoutParams as StaggeredGridLayoutManager.LayoutParams).apply {
                    isFullSpan = true
                }
                binding.root.layoutParams = params
                DetailViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(getItemViewType(position)) {
            VIEW_TAG_CALENDAR_HEADER -> {
                calendarList[position].date?.let { date ->
                    (holder as HeaderViewHolder).onBind(date)
                }
            }
            VIEW_TAG_CALENDAR_GRID -> {
                calendarList[position].light?.let { light ->
                    (holder as GridViewHolder).onBind(light)
                }
            }
            else -> {
                calendarList[position].date?.let { date ->
                    (holder as DetailViewHolder).onBind(date)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return calendarList.size
    }

    // Adapter Config End

    // ViewHolders Start

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

        init {
            binding.root.setOnClickListener { view ->
                calendarList[layoutPosition].light?.let { light ->
                    onGridClicked(light.dateCode, getDetailViewPosition())
                }
            }
        }

        fun onBind(item: Light) {
            binding.apply {
                light = item
                vm = viewModel
                executePendingBindings()
            }
        }

        private fun getDetailViewPosition(): Int {
            var notGridPositionFirst = layoutPosition
            var notGridPositionEnd = layoutPosition

            while(calendarList[notGridPositionFirst].viewType == ViewType.CALENDAR_GRID) {
                notGridPositionFirst--
            }
            while(notGridPositionEnd < calendarList.size && calendarList[notGridPositionEnd].viewType == ViewType.CALENDAR_GRID) {
                notGridPositionEnd++
            }

            val seventhPosition = notGridPositionEnd - 1 - ((notGridPositionEnd - 1 - notGridPositionFirst) % 7)
            val dif = if(seventhPosition - layoutPosition >= 0) {
                seventhPosition - layoutPosition
            } else {
                7 + seventhPosition - layoutPosition
            }

            val isOverSingleLine = notGridPositionEnd - notGridPositionFirst > 7
            val detailPosition = if(!isOverSingleLine) {
                notGridPositionEnd
            } else {
                if(layoutPosition + dif >= notGridPositionEnd) {
                    notGridPositionEnd
                } else {
                    layoutPosition + 1 + dif
                }
            }

            return detailPosition
        }

    }

    inner class DetailViewHolder(val binding: ItemTagCalendarDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBind(dateCode: String) {
            GlobalScope.launch {
                viewModel.getLightWithTags(dateCode)
                viewModel.lightWithTags.value?.let {
                    GlobalScope.launch(Dispatchers.Main) {
                        binding.apply {
                            tvDate.text = it.light.dateCode
                            tvBrightness.text = it.light.bright.toString()
                            tvTags.text = "태그^^"
                            tvMemo.text = it.light.memo
                        }
                    }
                }
            }
        }
    }

    // ViewHolders End
}