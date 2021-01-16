package com.bd.bdproject.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bd.bdproject.R
import com.bd.bdproject.`interface`.OnTagClickListener
import com.bd.bdproject.`interface`.OnTagDeleteButtonClickListener
import com.bd.bdproject.data.model.Tag
import com.bd.bdproject.databinding.ItemTagBinding
import com.bd.bdproject.util.BitDamApplication.Companion.applicationContext

class TagAdapter: ListAdapter<Tag, TagAdapter.TagViewHolder>(TagDiffCallback()) {

    var brightness: Int? = null
    var isFilled: Boolean = false

    // 태그 변경 시 필요한 flag 및 temp 스트링 프로퍼티
    var isEditMode: Boolean = false
    var editModeTag: String? = null

    var onTagClickListener: OnTagClickListener? = null
    var onTagDeleteButtonClickListener: OnTagDeleteButtonClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        return TagViewHolder(ItemTagBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TagViewHolder(private val binding: ItemTagBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                layoutTag.setOnClickListener {
                    onTagClickListener?.onClick(getItem(adapterPosition).name)
                }

                btnDeleteTag.setOnClickListener {
                    onTagDeleteButtonClickListener?.onClick(getItem(adapterPosition).name)
                }
            }
        }

        fun bind(item: Tag) {
            binding.apply {
                tag = item
                executePendingBindings()

                setBackground(brightness, isFilled, item.name)
                setTextColor(brightness, item.name)
                setDeleteTagVisibility(item.name)
            }
        }

        private fun setBackground(brightness: Int?, isFilled: Boolean, tagName: String) {
            if(isFilled) {
                binding.tvTag.setBackgroundResource(R.drawable.deco_tag_default_or_selected)
            } else {
                when(brightness) {
                    in 0 until 80 -> {
                        if(isEditMode) {
                            binding.tvTag.setBackgroundResource(
                                if(tagName == editModeTag) {
                                    R.drawable.deco_tag_transparent_white
                                } else {
                                    R.drawable.deco_tag_transparent_white_not_edit
                                }
                            )
                        } else {
                            binding.tvTag.setBackgroundResource(R.drawable.deco_tag_transparent_white)
                        }
                    }
                    else -> {
                        if(isEditMode) {
                            binding.tvTag.setBackgroundResource(
                                if(tagName == editModeTag) {
                                    R.drawable.deco_tag_transparent_black
                                } else {
                                    R.drawable.deco_tag_transparent_black_not_edit
                                }
                            )
                        } else {
                            binding.tvTag.setBackgroundResource(R.drawable.deco_tag_transparent_black)
                        }
                    }
                }
            }
        }

        private fun setTextColor(brightness: Int?, tagName: String) {
            when(brightness) {
                in 0 until 80 -> {
                    if (isEditMode) {
                        binding.tvTag.setTextColor(
                            if (tagName == editModeTag) {
                                ContextCompat.getColor(applicationContext(), R.color.white)
                            } else {
                                Color.parseColor("#60FFFFFF")
                            }
                        )
                    } else {
                        binding.tvTag.setTextColor(ContextCompat.getColor(applicationContext(), R.color.white))
                    }
                }
                else -> {
                    if (isEditMode) {
                        binding.tvTag.setTextColor(
                            if (tagName == editModeTag) {
                                ContextCompat.getColor(applicationContext(), R.color.black)
                            } else {
                                Color.parseColor("#60000000")
                            }
                        )
                    } else {
                        binding.tvTag.setTextColor(ContextCompat.getColor(applicationContext(), R.color.black))
                    }
                }
            }
        }

        private fun setDeleteTagVisibility(tagName: String) {
            binding.apply {
                if(isEditMode && editModeTag == tagName) btnDeleteTag.visibility = View.VISIBLE
                else btnDeleteTag.visibility = View.GONE
            }
        }

    }

    fun submitList(list: MutableList<Tag>?, brightness: Int, isFilled: Boolean = false) {
        super.submitList(list)

        this.brightness = brightness
        this.isFilled = isFilled

        notifyDataSetChanged()
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