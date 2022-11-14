package com.example.composecalendar

data class Day(
    val day: Int,
    val month: Int,
    val year: Int,
    val dayOfWeek: Int,// Starting from Sunday
    val isToday: Boolean,
    val isCurrentMonth: Boolean
)
