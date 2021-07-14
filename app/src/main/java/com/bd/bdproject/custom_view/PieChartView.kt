package com.bd.bdproject.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.bd.bdproject.common.addTransparency
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
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 40f
    }

    var rect = RectF()

    // 밝기 분류 (1~5), cnt
    var datas = mutableListOf<Pair<Int, Int>>()
    var theMostCountBrightnessItem = -1
    var isOnlyOneData = false

    private var pieOffset = 0f
    private var unit = 0f
    private val divideCnt = 30


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paintOutLine.strokeWidth = width * 0.008f
        pieOffset = width * 0.032f
        unit = pieOffset / divideCnt
        paintBlur.strokeWidth = unit

        val rightEnd = width.toFloat() - pieOffset
        val bottomEnd = height.toFloat() - pieOffset

        drawCircle(canvas)

        rect.set(pieOffset, pieOffset, rightEnd, bottomEnd)
        drawCharts(canvas, true, paintIn)
        drawCharts(canvas, false, paintOutLine)

        for(i in 1..divideCnt) {
            val more = unit * i
            val degree = (Math.PI / 180) *  3 * (50 - (0.5 * i))
            rect.set(pieOffset - more, pieOffset - more, rightEnd + more, bottomEnd + more)
            //drawCharts(canvas, false, paintBlur, "%02d".format((50 * sin(degree)).toInt()))
            drawCharts(canvas, false, paintBlur, "%02d".format(60 - 2*i))
            rect.set(pieOffset + more, pieOffset + more, rightEnd - more, bottomEnd - more)
            //drawCharts(canvas, false, paintBlur, "%02d".format((50 * sin(degree)).toInt()))
            drawCharts(canvas, false, paintBlur, "%02d".format(60 - 2*i))
        }
    }

    private fun drawCircle(canvas: Canvas) {
        paintIn.color = Color.parseColor("#05FFFFFF")
        canvas.drawCircle(width / 2f, height / 2f, (width - 2 * pieOffset) / 2, paintIn)
    }

    private fun drawCharts(canvas: Canvas, useCenter: Boolean, paint: Paint, transparency: String? = null) {
        if(datas.isNullOrEmpty()) {
            return
        }

        val additionalCode = when(paint) {
            paintIn -> "66"
            paintBlur -> transparency!!
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

            // region text
            if(paint == paintIn) {
                val temp = startAngle + swipeAngle / 2f
                val centerAngle = if(temp >= 360f) temp - 360 else temp
                val percentage = (((datas[i].second.toFloat() / entireCount.toFloat()) * 100).roundToInt()).toString()

                Log.d("angle_test", "centerAngle: $centerAngle")

                // 4% 이상인 데이터만 숫자로 표시
                if(percentage.toInt() >= 4) {

                    // 가장 많이 등록된 항목은 글자크기를 크게
                    if(datas[i].first == theMostCountBrightnessItem) {
                        paintText.textSize = 60f
                    } else {
                        paintText.textSize = 40f
                    }

                    // 만약 데이터가 단 한개만 존재한다면 글자를 중앙배치
                    // 그렇지 않다면, 글자는 그래프 각 영역의 중간

                    val textMeasured = paintText.measureText("$percentage%", 0, percentage.length + 1)
                    val heightMeasured = paintText.descent() + paintText.ascent()

                    if(isOnlyOneData) {
                        canvas.drawText("$percentage%",
                            width/2f - (textMeasured / 2f),
                            height/2f - (heightMeasured / 2),
                            paintText
                        )
                    } else {
                        canvas.drawText("$percentage%",
                            width/2f + calculatePointX((width - 80f)/2, centerAngle) - (textMeasured / 2f),
                            height/2f + calculatePointY((height - 80f)/2, centerAngle) - (heightMeasured / 2),
                            paintText
                        )
                    }
                }

            }
            // endregion

            // 다음 그림 준비
            val temp = startAngle + swipeAngle
            startAngle = if(temp >= 360) temp - 360 else temp
        }
    }

    fun setData(data: MutableList<Pair<Int, Int>>) {
        this.datas = data

        if(data.isNotEmpty()) {
            data.sortByDescending { it.second }
            theMostCountBrightnessItem = data[0].first

            var count = 0
            for(i in data) {
                if(i.second != 0) {
                    count++
                }
            }
            isOnlyOneData = (count == 1)
        }

        invalidate()
    }

    private fun calculatePointX(radius: Float, angleInDegrees: Float): Float {
        return ((radius * 0.60) * Math.cos(angleInDegrees * Math.PI / 180f)).toFloat()
    }

    private fun calculatePointY(radius: Float, angleInDegrees: Float): Float {
        return ((radius * 0.60) * Math.sin(angleInDegrees * Math.PI / 180f)).toFloat()
    }
}