package com.bd.bdproject.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.data.model.DBInfo
import com.bd.bdproject.databinding.ItemCombineTagBinding
import com.bd.bdproject.databinding.ItemSelectorDbBinding


class DBAdapter(val list: List<DBInfo>): RecyclerView.Adapter<DBAdapter.DBViewHolder>() {

    var oldPos: Int? = null
    var newPos: MutableLiveData<Int?> = MutableLiveData(null)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DBViewHolder {
        return DBViewHolder(ItemSelectorDbBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: DBViewHolder, position: Int) {
        holder.onBind(list[position].name)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class DBViewHolder(val binding: ItemSelectorDbBinding): RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: String) {
            binding.apply {
                tvDb.text = item

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