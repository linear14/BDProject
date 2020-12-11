package com.bd.bdproject.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tag (
    @PrimaryKey val name: String
) {}