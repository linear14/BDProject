package com.bd.bdproject.common

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


    // Helper
    var firstInEnrollHome: Boolean
        get() = pref.getBoolean("FIRST_IN_ENROLL_HOME", true)
        set(value) = pref.edit().putBoolean("FIRST_IN_ENROLL_HOME", value).apply()

    var firstInEnrollTag: Boolean
        get() = pref.getBoolean("FIRST_IN_ENROLL_TAG", true)
        set(value) = pref.edit().putBoolean("FIRST_IN_ENROLL_TAG", value).apply()

    var firstInEnrollMemo: Boolean
        get() = pref.getBoolean("FIRST_IN_ENROLL_MEMO", true)
        set(value) = pref.edit().putBoolean("FIRST_IN_ENROLL_MEMO", value).apply()



    var firstInCollection: Boolean
        get() = pref.getBoolean("FIRST_IN_COLLECTION", true)
        set(value) = pref.edit().putBoolean("FIRST_IN_COLLECTION", value).apply()

    var firstInStatistic: Boolean
        get() = pref.getBoolean("FIRST_IN_STATISTIC", true)
        set(value) = pref.edit().putBoolean("FIRST_IN_STATISTIC", value).apply()

    var firstInManageHash: Boolean
        get() = pref.getBoolean("FIRST_IN_MANAGE_HASH", true)
        set(value) = pref.edit().putBoolean("FIRST_IN_MANAGE_HASH", value).apply()
}