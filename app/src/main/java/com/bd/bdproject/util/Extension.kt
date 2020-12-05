package com.bd.bdproject.util

import android.view.View
import android.view.ViewPropertyAnimator

fun View.animateTransparency(toAlpha: Float, duration: Long = 0): ViewPropertyAnimator {
    return this.animate()
        .alpha(toAlpha)
        .setDuration(duration)
}