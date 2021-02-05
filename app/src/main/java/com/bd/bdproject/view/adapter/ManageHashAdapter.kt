package com.bd.bdproject.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.data.model.TagWithLights
import com.bd.bdproject.databinding.ItemManageHashBinding
import com.bd.bdproject.dialog.BottomSelector
import com.bd.bdproject.view.activity.ManageHashActivity

class ManageHashAdapter(
    var tags: List<TagWithLights>,
    val checkBoxClickedListener: (TagWithLights) -> Unit,
    val bottomSelectorClickedListener: (Tag) -> Unit
): RecyclerView.Adapter<ManageHashAdapter.HashViewHolder>() {

    // TODO viewmodel에서 받아올 수 있는 방법을 생각해봐야겠음 (같은 정보인데 두개의 set 프로퍼티를 따로 관리하는게 맘에 안든다..)
    val checkedTags = mutableSetOf<TagWithLights>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashViewHolder {
        return HashViewHolder(ItemManageHashBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: HashViewHolder, position: Int) {
        holder.onBind(tags[position])
    }

    override fun getItemCount(): Int {
        return tags.size
    }

    fun removeAllCheckedPosition() {
        checkedTags.clear()
        notifyDataSetChanged()
    }

    inner class HashViewHolder(val binding: ItemManageHashBinding): RecyclerView.ViewHolder(binding.root) {

        fun onBind(twl: TagWithLights) {
            binding.apply {
                setCheckedState(twl)

                tvTag.text = twl.tag.name
                ivCheck.setOnClickListener {
                    if(twl in checkedTags) {
                        // 체크가 되어있는 상태 -> 체크 없애야함
                        checkedTags.remove(twl)
                        ivCheck.setBackgroundColor(Color.parseColor("#aaaaaa"))
                    } else {
                        // 체크가 안되어있는 상태 -> 체크 만들어줌
                        checkedTags.add(twl)
                        ivCheck.setBackgroundColor(Color.parseColor("#000000"))
                    }

                    checkBoxClickedListener(twl)
                }

                ivMore.setOnClickListener {
                    bottomSelectorClickedListener(twl.tag)
                }
            }
        }

        private fun setCheckedState(tag: TagWithLights) {
            if(tag in checkedTags) {
                binding.ivCheck.setBackgroundColor(Color.parseColor("#000000"))
            } else {
                binding.ivCheck.setBackgroundColor(Color.parseColor("#aaaaaa"))
            }
        }

    }
}