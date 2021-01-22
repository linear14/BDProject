package com.bd.bdproject.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.data.model.StatisticTagResult
import com.bd.bdproject.databinding.ItemStatisticTagResultBinding

class StatisticTagAdapter(private val onClick: (String) -> Unit): ListAdapter<StatisticTagResult, StatisticTagAdapter.MyHashViewHolder>(StatisticTagResultDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHashViewHolder {
        return MyHashViewHolder(ItemStatisticTagResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHashViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MyHashViewHolder(val binding: ItemStatisticTagResultBinding): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.tvHash.setOnClickListener {
                onClick(getItem(layoutPosition).name)
            }
        }
        fun bind(item: StatisticTagResult) {
            binding.apply {

                tvHash.text = "# ${item.name}"
                tvAvgBrightness.text = item.avg.toString()
                tvCnt.text = item.cnt.toString()
            }
        }
    }
}

class StatisticTagResultDiffCallback: DiffUtil.ItemCallback<StatisticTagResult>() {
    override fun areItemsTheSame(oldItem: StatisticTagResult, newItem: StatisticTagResult): Boolean {
        return (oldItem.name == newItem.name) && (oldItem.avg == newItem.avg) && (oldItem.cnt == newItem.cnt)
    }

    override fun areContentsTheSame(oldItem: StatisticTagResult, newItem: StatisticTagResult): Boolean {
        return oldItem == newItem
    }
}