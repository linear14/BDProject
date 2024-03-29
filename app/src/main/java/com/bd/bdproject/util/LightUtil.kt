package com.bd.bdproject.util

import android.graphics.Color
import android.util.Log
import com.bd.bdproject.data.model.LightRGB

object LightUtil {
    private val levelMap = mutableMapOf<Int, Pair<LightRGB, LightRGB>>().apply {
        this[0] = Pair(LightRGB(0, 0, 0), LightRGB(44, 16, 25))
        this[1] = Pair(LightRGB(0, 0, 0), LightRGB(80, 24, 41))
        this[2] = Pair(LightRGB(0, 0, 0), LightRGB(105, 48, 48))
        this[3] = Pair(LightRGB(0, 0, 0), LightRGB(122, 65, 65))
        this[4] = Pair(LightRGB(21, 1, 11), LightRGB(135, 87, 76))
        this[5] = Pair(LightRGB(21, 1, 11), LightRGB(150, 108, 99))
        this[6] = Pair(LightRGB(37, 4, 13), LightRGB(178, 142, 123))
        this[7] = Pair(LightRGB(56, 9, 21), LightRGB(200, 164, 143))
        this[8] = Pair(LightRGB(78, 24, 24), LightRGB(232, 193, 171))
        this[9] = Pair(LightRGB(108, 43, 22), LightRGB(242, 202, 180))
        this[10] = Pair(LightRGB(161, 64, 33), LightRGB(238, 201, 180))
        this[11] = Pair(LightRGB(201, 69, 27), LightRGB(255, 214, 191))
        this[12] = Pair(LightRGB(223, 80, 19), LightRGB(255, 211, 186))
        this[13] = Pair(LightRGB(232, 83, 0), LightRGB(255, 228, 195))
        this[14] = Pair(LightRGB(255, 107, 0), LightRGB(255, 232, 198))
        this[15] = Pair(LightRGB(255, 122, 0), LightRGB(255, 232, 198))
        this[16] = Pair(LightRGB(255, 138, 0), LightRGB(255, 232, 198))
        this[17] = Pair(LightRGB(255, 160, 17), LightRGB(255, 237, 192))
        this[18] = Pair(LightRGB(255, 184, 0), LightRGB(255, 242, 210))
        this[19] = Pair(LightRGB(255, 198, 51), LightRGB(255, 243, 212))
        this[20] = Pair(LightRGB(255, 205, 77), LightRGB(255, 245, 220))
    }

    fun getDiagonalLight(progress: Int? = null): IntArray {
        return when {
            progress != null && progress != 200 -> {
                getLights(
                    levelMap[progress / 10]!!,
                    levelMap[(progress / 10) + 1]!!,
                    progress * 10
                )
            }
            progress == null -> {
                intArrayOf(
                    Color.rgb(0, 0, 0),
                    Color.rgb(31, 43, 59)
                )
            }
            else -> {
                val lightMaxStart = levelMap[20]!!.first
                val lightMaxEnd = levelMap[20]!!.second

                intArrayOf(
                    Color.rgb(lightMaxStart.r, lightMaxStart.g, lightMaxStart.b),
                    Color.rgb(lightMaxEnd.r, lightMaxEnd.g, lightMaxEnd.b)
                )
            }
        }
    }

    /***
     *  EnrollHomeFragment <-> EnrollBrightnessFragment 사이의 빛을 만들어줄때 사용한다.
     *  @param totalTime 전체 애니메이션 시간
     *  @param currentTime 현재 애니메이션 시간 위치
     *  @param isReversed false면 검정 -> 별밤색, true면 별밤색 -> 검정색
     */
    fun getOutRangeLight(totalTime: Long, currentTime: Long, isReversed: Boolean, firstProgress: Int = 0): IntArray {
        // progress를 0 ~ 10 사이의 값으로 설정
        val ratio = ((currentTime / totalTime.toDouble()) * 100).toInt() // 0.681 -> 68
        if(ratio == 100) {
            return if(isReversed) {
                intArrayOf(
                    Color.rgb(0, 0, 0),
                    Color.rgb(31, 43, 59)
                )
            } else {
                val darkStart = levelMap[0]!!.first
                val darkEnd = levelMap[0]!!.second

                intArrayOf(
                    Color.rgb(darkStart.r, darkStart.g, darkStart.b),
                    Color.rgb(darkEnd.r, darkEnd.g, darkEnd.b)
                )
            }
        }

        return if(isReversed) {
            getLights(levelMap[firstProgress]!!, Pair(LightRGB(0, 0, 0), LightRGB(31, 43, 59)), ratio)
        } else {
            getLights(Pair(LightRGB(0, 0, 0), LightRGB(31, 43, 59)), levelMap[0]!!, ratio)
        }
    }

    /***
     *  @return IntArray(first, second)
     *  first: 시작 빛의 Color.rgb 코드값
     *  second: 종료 빛의 Color.rgb 코드값
     */
    private fun getLights(start: Pair<LightRGB, LightRGB>, end: Pair<LightRGB, LightRGB>, progress: Int): IntArray {
        val progressRatio = ((progress % 100).toDouble() / 100)

        val redStart = start.first.r + ((end.first.r - start.first.r) * progressRatio).toInt()
        val greenStart = start.first.g + ((end.first.g - start.first.g) * progressRatio).toInt()
        val blueStart = start.first.b + ((end.first.b - start.first.b) * progressRatio).toInt()

        val redEnd = start.second.r + ((end.second.r - start.second.r) * progressRatio).toInt()
        val greenEnd = start.second.g + ((end.second.g - start.second.g) * progressRatio).toInt()
        val blueEnd = start.second.b + ((end.second.b - start.second.b) * progressRatio).toInt()

        return intArrayOf(Color.rgb(redStart, greenStart, blueStart), Color.rgb(redEnd, greenEnd, blueEnd))
    }
}