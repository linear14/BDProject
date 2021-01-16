package com.bd.bdproject.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Light (
    // 201212, 210114 와 같은 방식 ("yyMMdd")
    @PrimaryKey val dateCode: String,
    val bright: Int,    // todo 출시할 때 brightness로 바꾸자
    val memo: String?
): Parcelable {}