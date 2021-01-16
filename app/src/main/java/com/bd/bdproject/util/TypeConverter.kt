package com.bd.bdproject.util

import androidx.room.TypeConverter
import java.text.SimpleDateFormat

class TypeConverter {

    @TypeConverter
    fun timeToString(timeStamp: Long?): String? {
        return timeStamp?.let { FORMATTER.format(timeStamp) }
    }

    @TypeConverter
    fun timeToLong(timeStamp: String?): Long? {
        return timeStamp?.let { FORMATTER.parse(it)?.time }
    }

    companion object{
        val FORMATTER = SimpleDateFormat("yyyyMMdd")
    }
}