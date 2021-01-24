package com.bd.bdproject.`interface`

interface OnCalendarItemClickedListener {
    fun onGridClicked(dateCode: String, position: Int)
    fun onDetailClosed(position: Int)
}