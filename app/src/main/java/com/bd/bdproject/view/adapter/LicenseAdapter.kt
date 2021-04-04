package com.bd.bdproject.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.data.model.License
import com.bd.bdproject.databinding.ItemLicenseBinding
import com.bd.bdproject.util.Licenses

class LicenseAdapter: RecyclerView.Adapter<LicenseAdapter.LicenseViewHolder>() {

    val items = Licenses.items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
        return LicenseViewHolder(ItemLicenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
        holder.onBind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class LicenseViewHolder(val binding: ItemLicenseBinding): RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: License) {
            binding.apply {
                tvLicenseTitle.text = item.title
                tvLicenseDescription.text = item.description
                tvLicenseDetail.text = item.detail
            }
        }
    }
}