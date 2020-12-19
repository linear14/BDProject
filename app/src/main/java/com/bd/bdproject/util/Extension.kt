package com.bd.bdproject.util

import android.view.View
import android.view.ViewPropertyAnimator
import com.bd.bdproject.data.TypeConverter.Companion.FORMATTER
import java.lang.Exception
import java.lang.StringBuilder

fun View.animateTransparency(toAlpha: Float, duration: Long = 0): ViewPropertyAnimator {
    return this.animate()
        .alpha(toAlpha)
        .setDuration(duration)
}

fun Long.timeToString(): String {
    return this.let { FORMATTER.format(this) }
}

fun String.timeToLong(): Long {
    return this.let { FORMATTER.parse(it).time }
}

/***
 *  "yyyyMMdd" 형식의 String을 "December 19" 와 같은 format으로 변환시켜주는 함수
 */
fun String.toBitDamDateFormat(): String {
    if(this.length != 8) return this
    try {
        this.toLong()
    } catch(e: Exception) { return this }

    val month = this.substring(startIndex = 4,  endIndex = 6).toInt()
    val day = this.substring(startIndex = 6, endIndex = 8).toInt()

    val stringBuilder = StringBuilder().apply {
        when(month) {
            1 -> append("January")
            2 -> append("February")
            3 -> append("March")
            4 -> append("April")
            5 -> append("May")
            6 -> append("June")
            7 -> append("July")
            8 -> append("August")
            9 -> append("September")
            10 -> append("October")
            11 -> append("November")
            12 -> append("December")
        }
        append(" ")
        append(day)
    }

    return stringBuilder.toString()
}