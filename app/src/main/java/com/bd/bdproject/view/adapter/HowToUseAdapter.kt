package com.bd.bdproject.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.data.model.Description
import com.bd.bdproject.data.raw.descriptions
import com.bd.bdproject.databinding.ItemHowToUseBinding

class HowToUseAdapter: RecyclerView.Adapter<HowToUseAdapter.HowToUseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HowToUseViewHolder {
        return HowToUseViewHolder(ItemHowToUseBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HowToUseViewHolder, position: Int) {
        holder.onBind(descriptions[position])
    }

    override fun getItemCount(): Int = descriptions.size


    inner class HowToUseViewHolder(val binding: ItemHowToUseBinding): RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: Description) {
            binding.ivHowToUse.setImageResource(item.res)
        }
    }
}




