package com.bd.bdproject.custom_view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bd.bdproject.R
import com.bd.bdproject.common.animateTransparency
import com.bd.bdproject.common.screenTransitionAnimationMilliSecond
import com.bd.bdproject.databinding.LayoutTooltipBinding

class ToolTip: ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ToolTip)

        try {
            toolTipText = typedArray.getString(R.styleable.ToolTip_text)?:""
            direction = typedArray.getInt(R.styleable.ToolTip_direction,2)
            initView()
        } finally {
            typedArray.recycle()
        }

    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        initView()
    }

    var binding: LayoutTooltipBinding = LayoutTooltipBinding.inflate(LayoutInflater.from(context), this)
    var toolTipText: String = ""
    var direction: Int = 2

    private fun initView() {
        visibility = View.GONE
        alpha = 0.0f
        binding.tvTooltip.text = toolTipText

        when(direction) {
            0 -> {
                binding.ivTipUp.visibility = View.VISIBLE
                binding.ivTipBottom.visibility = View.GONE
            }
            1 -> {
                binding.ivTipUp.visibility = View.GONE
                binding.ivTipBottom.visibility = View.VISIBLE
            }
            2 -> {
                binding.ivTipUp.visibility = View.GONE
                binding.ivTipBottom.visibility = View.GONE
            }
        }
    }

    fun show() {
        animateTransparency(1.0f, screenTransitionAnimationMilliSecond)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)
                    visibility = View.VISIBLE
                }
            })

    }

    fun hide() {
        animateTransparency(0.0f, screenTransitionAnimationMilliSecond)
            .setListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    visibility = View.GONE
                }
            })
    }
}