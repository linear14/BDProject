package com.bd.bdproject.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity
@Parcelize
data class Tag (
    @PrimaryKey var name: String
): Parcelable {}

@Parcelize
class Tags: ArrayList<Tag>(), Parcelable