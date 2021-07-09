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

    const val INFO_BRIGHTNESS="BRIGHTNESS"
    const val INFO_LIGHT = "LIGHT"
    const val INFO_TAG = "TAG"
    const val INFO_DB = "DB"

    // 에러 났을경우
    const val ACTIVITY_NOT_RECOGNIZED = -1000
    const val DESTINATION_NOT_RECOGNIZED = -1000

    const val MAX_MEMO_LENGTH = 50


    // 위로멘트 (0, 5~35, 40~60, 65~85, 90~100)
    val ment0 = arrayOf(
        ""
    )
    val ment1 = arrayOf(
        "튼튼한 마음은 좋은 말에서 비롯된답니다",
        "오늘 하루 고생한 나를 다독여주세요",
        "나에게 여유를 주는건 어떨까요?"
    )
    val ment2 = arrayOf(
        "오늘은 내일의 징검다리가 될 거예요",
        "더 멋진 내일이 기다리고 있어요"
    )
    val ment3 = arrayOf(
        "오늘의 나에게 칭찬 한마디 어떨까요?",
        "빛나는 당신을 응원해요",
        "차근차근 잘 하고 있어요"
    )
    val ment4 = arrayOf(
        "오늘의 기분을 잘 간직해주세요",
        "밝은 모습이 보기 좋아요",
        "오늘의 따뜻함을 나눠줄 사람이 있나요?"
    )
    val mentToday: Array<Array<String>> = arrayOf(ment0, ment1, ment2, ment3, ment4)
}