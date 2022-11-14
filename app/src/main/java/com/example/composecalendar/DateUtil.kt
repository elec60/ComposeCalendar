package com.example.composecalendar

object DateUtil {
    private val monthNames = listOf(
        "January",
        "February",
        "March",
        "April",
        " May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )

    private val weekLetters = listOf(
        "S",
        "M",
        "T",
        "W",
        "T",
        "F",
        "S"
    )

    fun getMonthNameByIndex(index: Int) = monthNames[index]

    fun getWeekLetterByIndex(index: Int) = weekLetters[index]


}