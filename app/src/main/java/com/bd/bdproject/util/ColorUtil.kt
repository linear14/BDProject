package com.bd.bdproject.util

import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.bd.bdproject.R
import com.bd.bdproject.custom_view.MemoEditText
import com.bd.bdproject.common.BitDamApplication.Companion.applicationContext

object ColorUtil {

    fun setEntireViewColor(brightness: Int, vararg view: View) {
        val color = if(brightness in 0 until 80) R.color.white else R.color.black
        val colorCode = ContextCompat.getColor(applicationContext(), color)

        for(item in view) {
            when(item) {
                is MemoEditText -> {
                    item.setTextColor(colorCode)
                    item.setHintTextColor(colorCode)
                    item.setLineColor(colorCode)
                }
                is EditText -> {
                    item.setTextColor(colorCode)
                    item.setHintTextColor(Color.parseColor(if(brightness in 0 until 80) "#60FFFFFF" else "#60000000"))
                }
                is TextView -> {
                    item.setTextColor(colorCode)
                }
                is ImageView -> {
                    val drawable = DrawableCompat.wrap(item.drawable)
                    DrawableCompat.setTint(drawable.mutate(), colorCode)
                }
                else -> {
                    item.setBackgroundColor(colorCode)
                }
            }
        }
    }
}