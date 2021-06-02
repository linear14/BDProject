package com.bd.bdproject.util

import com.bd.bdproject.common.BitDamApplication

object SharedUtil {

    fun isAnimationActive(): Boolean = BitDamApplication.pref.isAnimationActivate

    fun isPasswordExist(): Boolean {
        val password = BitDamApplication.pref.bitdamPassword
        return password != null
    }
}