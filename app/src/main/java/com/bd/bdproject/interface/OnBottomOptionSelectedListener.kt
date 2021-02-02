package com.bd.bdproject.`interface`

import com.bd.bdproject.data.model.Tag

interface OnBottomOptionSelectedListener {
    fun onEdit(tag: Tag)
    fun onDelete(tag: Tag)
}