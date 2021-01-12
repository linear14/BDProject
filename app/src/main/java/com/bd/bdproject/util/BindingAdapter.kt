package com.bd.bdproject.util

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter

// todo 이 바인딩 어댑터에서, brightness도 받아서, 글자색을 밝기에 따라 바꿔도 될듯)
@BindingAdapter("tagName")
fun bindTagName(view: TextView, tagName: String) {
    view.text = "# $tagName"
}

@BindingAdapter("brightness")
fun bindBrightness(view: View, brightness: Int) {

    val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.TL_BR
    }
    gradientDrawable.colors = LightUtil.getDiagonalLight(brightness * 2)

    view.background = gradientDrawable

}

@BindingAdapter("dateCode")
fun bindDay(view: TextView, dateCode: String) {
    val day = dateCode.substring(6, 8)

    if(day[0] == '0') {
        view.text = day[1].toString()
    } else {
        view.text = day
    }

}
