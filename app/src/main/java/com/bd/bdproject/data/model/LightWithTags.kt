package com.bd.bdproject.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

// Light : TAG = 1 : N
data class LightWithTags(
    @Embedded val light: Light,
    @Relation(
        parentColumn = "dateCode",
        entityColumn = "name",
        associateBy = Junction(LightTagRelation::class)
    )
    val tags: List<Tag>
)