package com.bd.bdproject.data.model

import com.bd.bdproject.ViewType

data class TagCalendar(
    val viewType: ViewType,
    val date: String? = null,
    val light: Light? = null
) {
}