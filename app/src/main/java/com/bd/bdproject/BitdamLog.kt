package com.bd.bdproject

import android.util.Log
import com.bd.bdproject.util.timeToLong
import com.bd.bdproject.util.timeToString

object BitdamLog {

    fun titleLogger(msg: String) {
        Log.d("BITDAM_LOGGER_TITLE", "=============$msg=============")
    }

    fun contentLogger(msg: String) {
        Log.d("BITDAM_LOGGER_CONTENT", msg)
    }

    fun contentLogger(msg: Number) {
        Log.d("BITDAM_LOGGER_CONTENT", msg.toString())
    }

    fun dateCodeLogger(dateCode: Long?) {
        dateCode?.let {
            Log.d("BITDAM_LOGGER_DATECODE", "Long: $dateCode, String: ${dateCode.timeToString()}, TimeReturn: ${dateCode.timeToString().timeToLong()}")
        }?: Log.d("BITDAM_LOGGER_DATECODE", "null")
    }

    fun dateCodeLogger(dateCode: String?) {
        dateCode?.let {
            Log.d("BITDAM_LOGGER_DATECODE", "Long: ${dateCode.timeToLong()}, String: $dateCode")
        }?: Log.d("BITDAM_LOGGER_DATECODE", "null")
    }
}