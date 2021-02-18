package com.bd.bdproject.custom_view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.bd.bdproject.util.addTransparency
import kotlin.math.roundToInt

class PieChartView: View {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    private val paintIn = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintOutLine = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        // strokeCap = Paint.Cap.ROUND
    }

    private val paintBlur = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        maskFilter = BlurMaskFilter(20f, BlurMaskFilter.Blur.NORMAL)
    }

    private val paintText = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 40f
    }

    var rect = RectF()

    // 밝기 분류 (1~5), cnt
    var datas = mutableListOf<Pair<Int, Int>>()

    val layoutOffset = 20f
    val pieOffset = 40f



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paintOutLine.strokeWidth = width * 0.02f

        val rightEnd = width.toFloat() - pieOffset
        val bottomEnd = height.toFloat() - pieOffset

        rect.set(layoutOffset, layoutOffset, rightEnd + layoutOffset, bottomEnd + layoutOffset)
        drawCharts(canvas, true, paintBlur)

        rect.set(pieOffset, pieOffset, rightEnd, bottomEnd)
        drawCharts(canvas, true, paintIn)
        drawCharts(canvas, false, paintOutLine)
    }

    private fun drawCharts(canvas: Canvas, useCenter: Boolean, paint: Paint) {
        if(datas.isNullOrEmpty()) {
            return
        }

        val additionalCode = when(paint) {
            paintIn -> "70"
            paintBlur -> "70"
            else -> ""
        }
        var entireCount = 0
        var startAngle = 270f

        for(i in datas) {
            entireCount += i.second
        }

        for(i in datas.indices) {
            if(datas[i].second == 0) continue

            val swipeAngle = (360 * (datas[i].second.toFloat() / entireCount.toFloat()))

            // 1이 제일 어두움. 5가 제일 밝음
            when(datas[i].first) {
                1 -> {
                    paint.color = Color.parseColor("#2a0d16".addTransparency(additionalCode))
                }
                2 -> {
                    paint.color = Color.parseColor("#563737".addTransparency(additionalCode))
                }
                3 -> {
                    paint.color = Color.parseColor("#a14021".addTransparency(additionalCode))
                }
                4 -> {
                    paint.color = Color.parseColor("#ff7a00".addTransparency(additionalCode))
                }
                5 -> {
                    paint.color = Color.parseColor("#ffcd4d".addTransparency(additionalCode))
                }
            }
            canvas.drawArc(rect, startAngle, swipeAngle, useCenter, paint)

            // text test
            /*if(paint == paintIn) {
                val temp = startAngle + swipeAngle / 2f
                val centerAngle = if(temp >= 360f) temp - 360 else temp
                val percentage = (((datas[i].second.toFloat() / entireCount.toFloat()) * 1000).roundToInt() / 10.0).toString()

                Log.d("angle_test", "centerAngle: $centerAngle")

                val textMeasured = paintText.measureText(percentage, 0, percentage.length)
                val heightMeasured = paintText.descent() + paintText.ascent()

                canvas.drawText("$percentage%",
                    width/2f + calculatePointX((width - 80f)/2, centerAngle) - (textMeasured / 2f),
                    height/2f + calculatePointY((height - 80f)/2, centerAngle) - (heightMeasured / 2),
                    paintText
                )

                // 다음 텍스트 준비
                Log.d("angle_test", "swipeAngle: $swipeAngle")
            }*/
            // text test end

            // 다음 그림 준비
            val temp = startAngle + swipeAngle
            startAngle = if(temp >= 360) temp - 360 else temp
        }
    }

    fun setData(data: MutableList<Pair<Int, Int>>) {
        this.datas = data
        invalidate()
    }

    private fun calculatePointX(radius: Float, angleInDegrees: Float): Float {
        return ((radius * 0.55) * Math.cos(angleInDegrees * Math.PI / 180f)).toFloat()
    }

    private fun calculatePointY(radius: Float, angleInDegrees: Float): Float {
        return ((radius * 0.55) * Math.sin(angleInDegrees * Math.PI / 180f)).toFloat()
    }
}