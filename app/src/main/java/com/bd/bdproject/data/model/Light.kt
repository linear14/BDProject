package com.bd.bdproject.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Light (
    // 201212, 210114 와 같은 방식 ("yyMMdd")
    @PrimaryKey val dateCode: String,
    val bright: Int,
    val memo: String?
) {}