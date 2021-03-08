package com.bd.bdproject.util

import android.content.Context
import android.content.Context.MODE_PRIVATE

class BitdamSharedPreferences(context: Context) {
    val pref = context.getSharedPreferences("bitdam_pref", MODE_PRIVATE)

    var isShowDate: Boolean
        get() = pref.getBoolean("IS_SHOW_DATE", false)
        set(value) = pref.edit().putBoolean("IS_SHOW_DATE", value).apply()

    var isAnimationActivate: Boolean
        get() = pref.getBoolean("IS_ANIMATION_ACTIVATE", false)
        set(value) = pref.edit().putBoolean("IS_ANIMATION_ACTIVATE", value).apply()
}