package com.bd.bdproject.util

import android.view.View
import android.view.ViewPropertyAnimator
import com.bd.bdproject.data.TypeConverter.Companion.FORMATTER

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