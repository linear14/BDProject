package com.bd.bdproject.data.model

import com.bd.bdproject.StatisticViewType

data class StatisticCalendar(
    val type: StatisticViewType,
    val dateLong: Long? = null
) {}
