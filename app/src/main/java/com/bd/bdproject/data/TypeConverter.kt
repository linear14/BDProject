package com.bd.bdproject.data

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class TypeConverter {

    /*@TypeConverter
    fun dateToString(date: Date): String {
        val sdf = SimpleDateFormat("yyMMdd")
        return sdf.format(date)
    }

    @TypeConverter
    fun stringToDate(string: String): Date {
        val sdf = SimpleDateFormat("yyMMdd")
        return sdf.parse(string)!!
    }*/

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