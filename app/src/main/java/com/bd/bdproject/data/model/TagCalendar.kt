package com.bd.bdproject.data.model

import com.bd.bdproject.TagViewType

data class TagCalendar(
    val viewType: TagViewType,
    val date: String? = null,
    val light: Light? = null
) {
}