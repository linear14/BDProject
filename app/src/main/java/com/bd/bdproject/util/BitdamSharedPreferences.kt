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

    var lastVisitedTime: Long
        get() = pref.getLong("LAST_VISITED_TIME", System.currentTimeMillis())
        set(value) = pref.edit().putLong("LAST_VISITED_TIME", value).apply()

    var dairyAlarmHour: Int
        get() = pref.getInt("DAIRY_ALARM_HOUR", -1)
        set(value) = pref.edit().putInt("DAIRY_ALARM_HOUR", value).apply()

    var dairyAlarmMin: Int
        get() = pref.getInt("DAIRY_ALARM_MIN", -1)
        set(value) = pref.edit().putInt("DAIRY_ALARM_MIN", value).apply()

    var bitdamPassword: String?
        get() = pref.getString("BITDAM_PASSWORD", null)
        set(value) = pref.edit().putString("BITDAM_PASSWORD", value).apply()

    var passwordHint: String?
        get() = pref.getString("BITDAM_PASSWORD_HINT", null)
        set(value) = pref.edit().putString("BITDAM_PASSWORD_HINT", value).apply()

    var useDairyPush: Boolean
        get() = pref.getBoolean("USE_DAIRY_PUSH", false)
        set(value) = pref.edit().putBoolean("USE_DAIRY_PUSH", value).apply()

    var useAppPush: Boolean
        get() = pref.getBoolean("USE_APP_PUSH", true)
        set(value) = pref.edit().putBoolean("USE_APP_PUSH", value).apply()
}