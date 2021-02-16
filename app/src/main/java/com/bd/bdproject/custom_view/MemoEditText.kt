package com.bd.bdproject.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.bd.bdproject.BitdamLog

class MemoEditText: AppCompatEditText {

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):
            super(context, attrs, defStyleAttr)


    private val rect: Rect = Rect()
    private val paint: Paint = Paint()

    init {
        setBackgroundResource(android.R.color.transparent)
        setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8.0f,  resources.displayMetrics), 1.0f)
        paint.style = Paint.Style.STROKE
        paint.color = ContextCompat.getColor(rootView.context, android.R.color.black)
    }

    override fun onDraw(canvas: Canvas) {
        val count = 4
        BitdamLog.contentLogger("height: $height, lineHeight: $lineHeight, count: $count")

        var baseline = getLineBounds(0, rect)
        val gapBetweenLineAndText = 15
        height = baseline + lineHeight * 3 + gapBetweenLineAndText + 2

        for(i in 0 until count) {
            BitdamLog.contentLogger("baseLine: $baseline")
            canvas.drawLine(
                rect.left.toFloat(),
                (baseline + gapBetweenLineAndText).toFloat(),
                rect.right.toFloat(),
                (baseline + gapBetweenLineAndText).toFloat(),
                paint
            )
            baseline += lineHeight
        }

        super.onDraw(canvas)
    }

    fun setLineColor(color: Int) {
        paint.color = color
    }


}