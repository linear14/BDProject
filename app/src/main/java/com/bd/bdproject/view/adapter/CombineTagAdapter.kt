package com.bd.bdproject.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.databinding.ItemCombineTagBinding


class CombineTagAdapter(val list: List<String>): RecyclerView.Adapter<CombineTagAdapter.CombineTagViewHolder>() {

    var oldPos: Int? = null
    var newPos: MutableLiveData<Int?> = MutableLiveData(null)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CombineTagViewHolder {
        return CombineTagViewHolder(ItemCombineTagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CombineTagViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class CombineTagViewHolder(val binding: ItemCombineTagBinding): RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: String) {
            binding.apply {
                tvTag.text = "# $item"

                if(layoutPosition == newPos.value) {
                    root.setBackgroundColor(Color.parseColor("#EBEBEB"))
                } else {
                    root.setBackgroundColor(Color.WHITE)
                }

                root.setOnClickListener {
                    oldPos = newPos.value
                    newPos.value = layoutPosition

                    oldPos?.let{
                        notifyItemChanged(it)
                    }
                    newPos.value?.let {
                        notifyItemChanged(it)
                    }
                }
            }
        }

    }

}