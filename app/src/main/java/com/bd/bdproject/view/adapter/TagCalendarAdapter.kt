package com.bd.bdproject.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bd.bdproject.TagViewType
import com.bd.bdproject.`interface`.OnCalendarItemClickedListener
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.model.TagCalendar
import com.bd.bdproject.databinding.ItemTagCalendarDetailBinding
import com.bd.bdproject.databinding.ItemTagCalendarGridBinding
import com.bd.bdproject.databinding.ItemTagCalendarHeaderBinding
import com.bd.bdproject.viewmodel.StatisticDetailViewModel
import kotlinx.coroutines.runBlocking
import java.lang.StringBuilder

class TagCalendarAdapter(
    var calendarList: MutableList<TagCalendar> = mutableListOf(),
    val viewModel: StatisticDetailViewModel,
    val onCalendarItemClickedListener: OnCalendarItemClickedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TAG_CALENDAR_HEADER = 1
        const val VIEW_TAG_CALENDAR_GRID = 2
        const val VIEW_TAG_DETAIL = 3
    }

    // Adapter Config Start

    override fun getItemViewType(position: Int): Int {
        return when (calendarList[position].viewType) {
            TagViewType.CALENDAR_HEADER -> VIEW_TAG_CALENDAR_HEADER
            TagViewType.CALENDAR_GRID -> VIEW_TAG_CALENDAR_GRID
            TagViewType.CALENDAR_DETAIL -> VIEW_TAG_DETAIL
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
                when(viewModel.activatedDetailPosition) {
                    null -> viewModel.activatedGridPosition = layoutPosition

                    // layoutPosition이 activatedDetailPosition보다 큰 경우
                    in 0 until layoutPosition -> viewModel.activatedGridPosition = layoutPosition - 1

                    // layoutPosition이 activatedDetailPosition보다 작은 경우
                    else -> viewModel.activatedGridPosition = layoutPosition
                }

                calendarList[layoutPosition].light?.let { light ->
                    onCalendarItemClickedListener.onGridClicked(light.dateCode, getDetailViewPosition())
                }
            }
        }

        fun onBind(item: Light) {
            setBackgroundFilter()
            binding.apply {
                light = item
                vm = viewModel
                executePendingBindings()
            }
        }

        private fun getDetailViewPosition(): Int {
            var notGridPositionFirst = layoutPosition
            var notGridPositionEnd = layoutPosition

            while(calendarList[notGridPositionFirst].viewType == TagViewType.CALENDAR_GRID) {
                notGridPositionFirst--
            }
            while(notGridPositionEnd < calendarList.size && calendarList[notGridPositionEnd].viewType == TagViewType.CALENDAR_GRID) {
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

        private fun setBackgroundFilter() {
            when(viewModel.activatedGridPosition) {
                null -> binding.root.alpha = 1.0f
                layoutPosition -> binding.root.alpha = 1.0f
                else -> binding.root.alpha = 0.4f
            }
        }

    }

    inner class DetailViewHolder(val binding: ItemTagCalendarDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnClose.setOnClickListener {
                viewModel.activatedGridPosition = null
                onCalendarItemClickedListener.onDetailClosed(layoutPosition)
            }
        }

        fun onBind(dateCode: String) {
            runBlocking {
                val deferred = viewModel.getLightWithTags(dateCode)
                val lwt = deferred.await()
                binding.apply {
                    tvDate.text = makeDateText(lwt.light.dateCode)
                    tvBrightness.text = lwt.light.bright.toString()
                    tvTags.text = makeTagText(lwt.tags)
                    tvMemo.text = lwt.light.memo
                }
            }
        }

        private fun makeDateText(dateCode: String): String {
            val year = dateCode.substring(0, 4)
            var month = dateCode.substring(4, 6)
            var day = dateCode.substring(6, 8)

            if(month[0] == '0') {
                month = month[1].toString()
            }

            if(day[0] == '0') {
                day = day[1].toString()
            }

            return "${year}년 ${month}월 ${day}일"
        }

        private fun makeTagText(tags: List<Tag>): String {
            val sb = StringBuilder()

            tags.forEach {
                sb.append("#${it.name}  ")
            }

            return sb.toString()
        }
    }

    // ViewHolders End
}