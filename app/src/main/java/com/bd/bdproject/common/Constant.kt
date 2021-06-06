package com.bd.bdproject.common

object Constant {

    // 프래그먼트 코드 (for previous fragment)
    const val INFO_PREVIOUS_FRAGMENT = "PREVIOUS_FRAGMENT"
    const val INFO_DESTINATION ="DESTINATION"
    const val CONTROL_HOME = 8000
    const val CONTROL_BRIGHTNESS = 8100
    const val CONTROL_TAG = 8200
    const val CONTROL_MEMO = 8300

    const val INFO_PREVIOUS_ACTIVITY = "PREVIOUS_ACTIVITY"
    const val SPLASH = 7000
    const val BITDAM_ENROLL = 7100
    const val BITDAM_EDIT = 7200
    const val DETAIL = 7300
    const val COLLECTION_MAIN = 7400

    const val INFO_DATE_CODE = "DATE_CODE"
    const val INFO_SHOULD_HAVE_DRAWER = "SHOULD_HAVE_DRAWER"

    const val INFO_LIGHT = "LIGHT"
    const val INFO_TAG = "TAG"
    const val INFO_DB = "DB"

    // 에러 났을경우
    const val ACTIVITY_NOT_RECOGNIZED = -1000
    const val DESTINATION_NOT_RECOGNIZED = -1000

    const val MAX_MEMO_LENGTH = 50


    // 위로멘트 (0, 5~35, 40~60, 65~85, 90~100)
    val ment0 = arrayOf(
        "빛 0~0 사이의 멘트입니다."
    )
    val ment1 = arrayOf(
        "빛 5~35 사이의 멘트입니다."
    )
    val ment2 = arrayOf(
        "빛 40~60 사이의 멘트입니다."
    )
    val ment3 = arrayOf(
        "빛 65~85 사이의 멘트입니다."
    )
    val ment4 = arrayOf(
        "빛 90~100 사이의 멘트입니다."
    )
    val mentToday: Array<Array<String>> = arrayOf(ment0, ment1, ment2, ment3, ment4)
}