package com.bd.bdproject.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat

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

    override fun onDraw(canvas: Canvas?) {
        val lineCount = lineCount

        for(i in 0 until lineCount) {
            val baseline = getLineBounds(i, rect)

            canvas?.drawLine(
                rect.left.toFloat(),
                (baseline + 10).toFloat(),
                rect.right.toFloat(),
                (baseline + 10).toFloat(),
                paint
            )
        }

        super.onDraw(canvas)
    }

    fun setLineColor(color: Int) {
        paint.color = color
    }


}