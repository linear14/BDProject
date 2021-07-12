package com.bd.bdproject.custom_view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.bd.bdproject.R
import com.bd.bdproject.databinding.LayoutIndicatorBinding

class Indicator: LinearLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    var binding: LayoutIndicatorBinding = LayoutIndicatorBinding.inflate(LayoutInflater.from(context), this)
    var selectedPos: Int = 0
        set(value) {
            field = value
            handleIndicator()
        }

    init {
       handleIndicator()
    }

    private fun handleIndicator() {
        val parent = binding.root as LinearLayout
        for(i in 0 until parent.childCount) {
            if(i != selectedPos) {
                parent.getChildAt(i).setBackgroundResource(R.drawable.deco_indicator_unselect)
            } else {
                parent.getChildAt(i).setBackgroundResource(R.drawable.deco_indicator_select)
            }
        }
    }


}