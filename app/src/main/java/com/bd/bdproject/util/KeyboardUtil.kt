package com.bd.bdproject.util

import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.bd.bdproject.common.BitDamApplication.Companion.applicationContext

object KeyboardUtil {

    private val inputMethodManager by lazy { applicationContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager }

    // focusView 에 focus 를 두며 키보드가 보여짐
    fun keyBoardShowUp(focusView: View) {
        inputMethodManager.showSoftInput(focusView, 0)
    }

    // 만약 focusView 에 focus 가 잡혀있다면 키보드 내림
    fun keyBoardHide(focusView: View) {
        inputMethodManager.hideSoftInputFromWindow(focusView.windowToken, 0)
    }

}