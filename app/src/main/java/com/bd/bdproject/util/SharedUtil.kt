package com.bd.bdproject.util

object SharedUtil {

    fun isAnimationActive(): Boolean = BitDamApplication.pref.isAnimationActivate

    fun isPasswordExist(): Boolean {
        val password = BitDamApplication.pref.bitdamPassword
        return password != null
    }
}