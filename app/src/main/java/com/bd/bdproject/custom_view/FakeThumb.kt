package com.bd.bdproject.custom_view

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.bd.bdproject.R

class FakeThumb: View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    val thumbPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        flags = Paint.ANTI_ALIAS_FLAG
    }

    var thumbRadius: Float
        set(value) {
            field = value
            invalidate()
        }

    var verticalOffset: Float
        set(value) {
            field = value
            invalidate()
        }

    private val minValue: Int

    var maxValue: Int
        set(value) {
            field = value
            invalidate()
        }

    var thumbRect: Rect? = null
    init {
        thumbRadius = 20.dpToPx()
        verticalOffset = 25.dpToPx()
        minValue = 0
        maxValue = 200
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = thumbRadius * 2
        val height = verticalOffset * 2
        setMeasuredDimension(width.toInt(), height.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawThumb(canvas)
    }

    private fun drawThumb(canvas: Canvas) {
        if(thumbRect == null) {
            thumbRect = Rect(
                width / 2 - thumbRadius.toInt(),
                height / 2 - thumbRadius.toInt(),
                width / 2 + thumbRadius.toInt(),
                height / 2 + thumbRadius.toInt()
            )
        }

        thumbRect?.let {
            canvas.drawCircle((it.left + it.right) / 2f, (it.top + it.bottom) / 2f, thumbRadius, thumbPaint)
        }

    }

    fun Int.dpToPx(): Float {
        return (this * Resources.getSystem().displayMetrics.density)
    }

}