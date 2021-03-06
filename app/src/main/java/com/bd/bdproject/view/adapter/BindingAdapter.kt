package com.bd.bdproject.view.adapter

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bd.bdproject.util.LightUtil

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

@BindingAdapter("brightnessCollection")
fun bindBrightnessForCollection(view: View, brightness: Int) {

    val gradientDrawable = GradientDrawable().apply {
        orientation = GradientDrawable.Orientation.BR_TL
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
