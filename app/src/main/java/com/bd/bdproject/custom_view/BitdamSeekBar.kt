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

class BitdamSeekBar: View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    val barPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.transparent)
        flags = Paint.ANTI_ALIAS_FLAG
    }

    val thumbPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        flags = Paint.ANTI_ALIAS_FLAG
    }

    private var isInit = true

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

    private var valueBoundaryList: MutableList<Float>? = null

    private var defaultProgress: Int = 0
    var firstProgress: Int? = null

    var thumbRect: Rect? = null
    private var thumbPressed = false
    var thumbAvailable = false

    private var onProgressChangeListener: ((Int) -> Unit)? = null
    private var onPressListener: (() -> Unit)? = null
    private var onReleaseListener: (() -> Unit)? = null
    private var onThumbFirstClickListener: (() -> Unit)? = null

    init {
        thumbRadius = 20.dpToPx()
        verticalOffset = 25.dpToPx()
        minValue = 0
        maxValue = 200
        thumbPressed = false
        isInit = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBar(canvas)
        drawThumb(canvas)

        activateThumbTouchListener()

        if(isInit) {
            setThumbLevel(firstProgress?:defaultProgress)
            isInit = false
        }
    }

    private fun drawBar(canvas: Canvas) {
        canvas.drawRect(
            width / 2 - 1f,
            verticalOffset,
            width / 2 + 1f,
            (height - verticalOffset),
            barPaint
        )
    }

    private fun drawThumb(canvas: Canvas) {
        if(thumbRect == null) {
            thumbRect = Rect(
                width / 2 - thumbRadius.toInt(),
                height - verticalOffset.toInt() - thumbRadius.toInt(),
                width / 2 + thumbRadius.toInt(),
                height - verticalOffset.toInt() + thumbRadius.toInt()
            )
        }

        thumbRect?.let {
            canvas.drawCircle((it.left + it.right) / 2f, (it.top + it.bottom) / 2f, thumbRadius, thumbPaint)
        }

    }

    fun Int.dpToPx(): Float {
        return (this * Resources.getSystem().displayMetrics.density)
    }

    fun setOnProgressChangeListener(listener: ((Int) -> Unit)?) {
        this.onProgressChangeListener = listener
    }

    fun setOnPressListener(listener: (() -> Unit)?) {
        this.onPressListener = listener
    }

    fun setOnReleaseListener(listener: (() -> Unit)?) {
        this.onReleaseListener = listener
    }

    fun setOnThumbFirstClickListener(listener: (() -> Unit)?) {
        this.onThumbFirstClickListener = listener
    }

    private fun activateThumbTouchListener() {
        setOnTouchListener { view, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {

                    thumbRect?.let { thumbRect ->
                        if(thumbRect.contains(event.x.toInt(), event.y.toInt())) {
                            thumbPressed = true
                            onPressListener?.invoke()
                        }
                    }

                }

                MotionEvent.ACTION_MOVE -> {
                    if(valueBoundaryList == null) {
                        makeBoundaryList()
                    }

                    if(thumbPressed && thumbAvailable) {
                        thumbRect?.let { thumbRect ->
                            var (x, y) = event.x to event.y + top

                            if(y <= top + verticalOffset) {
                                y = top + verticalOffset
                                onProgressChangeListener?.invoke(maxValue)
                            } else if(y >= bottom - verticalOffset) {
                                y = bottom - verticalOffset
                                onProgressChangeListener?.invoke(minValue)
                            } else {
                                valueBoundaryList?.let { valueBoundaryList ->
                                    for(i in minValue .. maxValue) {
                                        if(i != maxValue) {
                                            if(y >= valueBoundaryList[i] && y < valueBoundaryList[i + 1]) {
                                                y = valueBoundaryList[i]
                                                onProgressChangeListener?.invoke(maxValue - i)
                                                break
                                            }
                                        } else {
                                            y = bottom - verticalOffset
                                            onProgressChangeListener?.invoke(minValue)
                                        }
                                    }
                                }

                            }

                            this.thumbRect = Rect(
                                width / 2 - thumbRadius.toInt(),
                                (y - top - thumbRadius).toInt(),
                                width / 2 + thumbRadius.toInt(),
                                (y - top + thumbRadius).toInt()
                            )

                            invalidate()
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if(thumbPressed) {
                        onReleaseListener?.invoke()
                        view.performClick()
                        /*if(thumbAvailable) {
                           onReleaseListener?.invoke()
                            view.performClick()
                        } else {
                            onThumbFirstClickListener?.invoke()
                            onProgressChangeListener?.invoke(firstProgress?:defaultProgress)
                        }*/
                    }
                    thumbPressed = false
                }
            }
            true
        }
    }

    fun setThumbLevel(level: Int) {
        if(valueBoundaryList == null) {
            makeBoundaryList()
        }

        val realLevel = maxValue - level

        valueBoundaryList?.let { valueBoundaryList ->
            thumbRect = Rect(
                width / 2 - thumbRadius.toInt(),
                (valueBoundaryList[realLevel] - top - thumbRadius).toInt(),
                width / 2 + thumbRadius.toInt(),
                (valueBoundaryList[realLevel] - top + thumbRadius).toInt()
            )
        }
        // onProgressChangeListener?.invoke(level)
        invalidate()
    }

    private fun makeBoundaryList() {
        valueBoundaryList = mutableListOf()

        val start = top + verticalOffset
        val end = bottom - verticalOffset
        val interval = (end - start) / maxValue

        for(i in minValue .. maxValue) {
            valueBoundaryList?.add(start + interval * i)
        }

        valueBoundaryList?.let { list ->
            if(list[maxValue] != end) {
                list[maxValue] = end
            }
        }
    }

    fun makeBarVisible() {
        barPaint.color = ContextCompat.getColor(context, R.color.white)
        invalidate()
    }

    fun makeThumbInVisible() {
        thumbPaint.color = ContextCompat.getColor(context, R.color.transparent)
        invalidate()
    }

    fun makeThumbVisible() {
        thumbPaint.color = ContextCompat.getColor(context, R.color.white)
        invalidate()
    }

    fun getThumbPositionRatio(progress: Int): Float {
        return 1- progress / maxValue.toFloat()
        // return progress / maxValue.toFloat()
    }

}