package com.bd.bdproject.data.model

import androidx.room.Entity

@Entity(primaryKeys = ["dateCode", "name"])
data class LightTagRelation(
    val dateCode: String,
    val name: String
)
