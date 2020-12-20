package com.bd.bdproject.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.BitDamApplication.Companion.applicationContext
import com.bd.bdproject.R
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.ItemTagBinding

class TagAdapter: ListAdapter<Tag, TagAdapter.TagViewHolder>(TagDiffCallback()) {

    var brightness: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        return TagViewHolder(ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.setBackground(brightness)
        holder.setTextColor(brightness)
    }

    inner class TagViewHolder(private val binding: ItemTagBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Tag) {
            binding.apply {
                tag = item
                executePendingBindings()
            }
        }

        fun setBackground(brightness: Int?) {
            when(brightness) {
                in 0 until 80 -> { binding.tvTag.setBackgroundResource(R.drawable.deco_tag_transparent_white)}
                else -> { binding.tvTag.setBackgroundResource(R.drawable.deco_tag_transparent_black)}
            }
        }

        fun setTextColor(brightness: Int?) {
            when(brightness) {
                in 0 until 80 -> { binding.tvTag.setTextColor(ContextCompat.getColor(applicationContext(), R.color.white)) }
                else -> { binding.tvTag.setTextColor(ContextCompat.getColor(applicationContext(), R.color.black)) }
            }
        }
    }

    fun submitList(list: MutableList<Tag>?, brightness: Int) {
        super.submitList(list)

        if(this.brightness != brightness) {
            this.brightness = brightness
            notifyDataSetChanged()
        }
    }
}

class TagDiffCallback: DiffUtil.ItemCallback<Tag>() {
    override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
        return oldItem == newItem
    }

}