package com.bd.bdproject.util

import android.content.Context
import android.content.Context.MODE_PRIVATE

class BitdamSharedPreferences(context: Context) {
    val pref = context.getSharedPreferences("bitdam_pref", MODE_PRIVATE)

    var isShowDate: Boolean
        get() = pref.getBoolean("IS_SHOW_DATE", false)
        set(value) = pref.edit().putBoolean("IS_SHOW_DATE", value).apply()

    var isAnimationActivate: Boolean
        get() = pref.getBoolean("IS_ANIMATION_ACTIVATE", true)
        set(value) = pref.edit().putBoolean("IS_ANIMATION_ACTIVATE", value).apply()

    var lastEnrolledLightTime: Long
        get() = pref.getLong("LAST_ENROLLED_LIGHT_TIME", System.currentTimeMillis())
        set(value) = pref.edit().putLong("LAST_ENROLLED_LIGHT_TIME", value).apply()

    var dairyAlarmTime: Long
        get() = pref.getLong("DAIRY_ALARM_TIME", 0)
        set(value) = pref.edit().putLong("DAIRY_ALARM_TIME", value).apply()

    var bitdamPassword: String?
        get() = pref.getString("BITDAM_PASSWORD", null)
        set(value) = pref.edit().putString("BITDAM_PASSWORD", value).apply()

    var passwordHint: String?
        get() = pref.getString("BITDAM_PASSWORD_HINT", null)
        set(value) = pref.edit().putString("BITDAM_PASSWORD_HINT", value).apply()
}