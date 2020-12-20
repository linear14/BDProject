package com.bd.bdproject.ui.main.adapter

import android.widget.TextView
import androidx.databinding.BindingAdapter

// todo 이 바인딩 어댑터에서, brightness도 받아서, 글자색을 밝기에 따라 바꿔도 될듯)
@BindingAdapter("tagName")
fun bindTagName(view: TextView, tagName: String) {
    view.text = "# $tagName"
}
