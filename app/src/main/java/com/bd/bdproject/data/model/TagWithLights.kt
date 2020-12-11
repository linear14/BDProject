package com.bd.bdproject.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

// Light : TAG = N : 1
data class TagWithLights(
    @Embedded val tag: Tag,
    @Relation(
        parentColumn = "name",
        entityColumn = "dateCode",
        associateBy = Junction(LightTagRelation::class)
    )
    val lights: List<Light>
)