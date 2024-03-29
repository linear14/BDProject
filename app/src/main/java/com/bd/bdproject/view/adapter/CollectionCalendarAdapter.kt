package com.bd.bdproject.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.databinding.ItemCalendarBinding

class CollectionCalendarAdapter(private val onClick: (String, Int) -> Unit): ListAdapter<Light, CollectionCalendarAdapter.CalendarViewHolder>(
    LightDiffUtil()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        return CalendarViewHolder(ItemCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CalendarViewHolder(private val binding: ItemCalendarBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Light) {
            binding.apply {
                light = item
                executePendingBindings()

                itemView.setOnClickListener { onClick(item.dateCode, item.bright) }
            }
        }
    }
}

class LightDiffUtil: DiffUtil.ItemCallback<Light>() {
    override fun areItemsTheSame(oldItem: Light, newItem: Light): Boolean {
        return oldItem.dateCode == newItem.dateCode
    }

    override fun areContentsTheSame(oldItem: Light, newItem: Light): Boolean {
        return oldItem == newItem
    }

}