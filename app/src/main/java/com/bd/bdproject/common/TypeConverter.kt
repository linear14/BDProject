package com.bd.bdproject.common

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
        val FORMATTER_TIME = SimpleDateFormat("HHmmss")
    }
}