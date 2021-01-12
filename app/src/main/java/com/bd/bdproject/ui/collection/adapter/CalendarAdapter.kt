package com.bd.bdproject.ui.collection.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.data.model.Light
import com.bd.bdproject.databinding.ItemCalendarBinding

class CalendarAdapter(private val onClick: (String) -> Unit): ListAdapter<Light, CalendarAdapter.CalendarViewHolder>(LightDiffUtil()) {

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

                itemView.setOnClickListener { onClick(item.dateCode) }
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