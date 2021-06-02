package com.bd.bdproject.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.R
import com.bd.bdproject.data.model.License
import com.bd.bdproject.databinding.ItemLicenseBinding
import com.bd.bdproject.common.Licenses
import com.bd.bdproject.common.dpToPx

class LicenseAdapter: RecyclerView.Adapter<LicenseAdapter.LicenseViewHolder>() {

    val items = Licenses.items
    val expandStates = Array<Boolean>(items.size) { false }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicenseViewHolder {
        return LicenseViewHolder(ItemLicenseBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LicenseViewHolder, position: Int) {
        holder.onBind(items[position], position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class LicenseViewHolder(val binding: ItemLicenseBinding): RecyclerView.ViewHolder(binding.root) {

        fun onBind(item: License, pos: Int) {
            binding.apply {
                tvLicenseTitle.text = item.title
                tvLicenseDescription.text = item.description
                tvLicenseDetail.text = item.detail

                val layoutParams = tvLicenseDetail.layoutParams as ConstraintLayout.LayoutParams
                if(!expandStates[pos]) {
                    ivExpandView.setImageResource(R.drawable.ic_baseline_add_24)
                    layoutParams.height = 180.dpToPx()
                } else {
                    ivExpandView.setImageResource(R.drawable.ic_baseline_remove_24)
                    layoutParams.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
                }
                tvLicenseDetail.layoutParams = layoutParams

                ivExpandView.setOnClickListener {
                    expandStates[pos] = !expandStates[pos]
                    notifyItemChanged(pos)
                }
            }
        }
    }
}