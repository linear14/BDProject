package com.bd.bdproject.common

import android.content.res.Resources
import android.view.View
import android.view.ViewPropertyAnimator
import com.bd.bdproject.data.model.SearchedTag
import com.bd.bdproject.common.TypeConverter.Companion.FORMATTER
import com.bd.bdproject.common.TypeConverter.Companion.FORMATTER_TIME
import java.text.SimpleDateFormat
import java.util.*

val screenTransitionAnimationMilliSecond = 750L

fun View.animateTransparency(toAlpha: Float, duration: Long = 0): ViewPropertyAnimator {
    return this.animate()
        .alpha(toAlpha)
        .setDuration(duration)
}

fun Long?.timeToString(): String {
    return this.let { FORMATTER.format(this) }
}

fun Long?.hmsToString(): String {
    return this.let { FORMATTER_TIME.format(this) }
}

fun String?.timeToLong(): Long {
    return this.let { FORMATTER.parse(it).time }
}

/***
 *  "2020년 3월 11일의 빛" 형태로 변환
 */
fun String.toFullDateToolbar(): String {
    if(this.length != 8) return this
    try {
        this.toLong()
    } catch(e: Exception) { return this }

    val currentTimeMillis = this.timeToLong()
    return SimpleDateFormat("yyyy년 M월 d일의 빛", Locale.KOREAN).format(currentTimeMillis)
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

/***
 *   "2020.04.03" 식의 형태로 변환
 */
fun String?.withDateSeparator(regex: String): String? {
    if(this == null || this.length != 8) return this

    val dateCode = this

    val sb = StringBuilder().apply {
        append(dateCode.substring(0, 4))
        append(regex)
        append(dateCode.substring(4, 6))
        append(regex)
        append(dateCode.substring(6, 8))
    }

    return sb.toString()
}

fun Long?.withDateSeparator(regex: String): String? {
    if(this == null) return this

    val dateCode = this.timeToString()
    return dateCode.withDateSeparator(regex)
}

fun Int.toLightLabel(): String {
    val start = if(this == 0) 0 else this * 20 + 1
    val end = this * 20 + 20
    return "$start ~ $end"
}

/***
 *  seekbar의 progress 값을 brightness 값으로 변환시켜주는 함수
 *  ex. (progress 값이 152일 경우 -> 75의 brightness 값으로 변홤시켜줌)
 */
fun Int?.convertToBrightness(): Int {
    if(this == null) return 0

    val converted = this / 10
    return (converted * 5)
}

fun Int.dpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun String.addTransparency(transparency: String): String {
    val oldCode = this

    return if(this.startsWith("#")) {
        val sb = StringBuilder().apply {
            append("#")
            append(transparency)
            append(oldCode.substring(1, oldCode.length))
        }
        sb.toString()
    } else {
        this
    }
}

fun MutableList<SearchedTag>.returnBoundaryList(maxCount: Int): MutableList<SearchedTag> {
    if(this.size < maxCount) {
        return this
    } else {
        val newList = mutableListOf<SearchedTag>()
        for(i in 0 until maxCount) {
            newList.add(this[i])
        }
        return newList
    }
}