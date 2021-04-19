package com.bd.bdproject.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DBInfo(
    val name: String,
    val id: String
): Parcelable
