@file:OptIn(ExperimentalFoundationApi::class)

package com.example.composecalendar

import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.util.*

val calendar: Calendar = Calendar.getInstance()
private const val COLUMNS_COUNT = 7

@Composable
fun CalendarUi(
    selectedDay: Day?,
    onDaySelected: (Day) -> Unit
) {
    val months = createMonthsData()

    val listState = rememberLazyListState()
    LaunchedEffect(key1 = true) {
        val todayIndex = months.indexOfFirst { month -> month.find { day -> day.isToday } != null }
        listState.scrollToItem(todayIndex)
    }

    LazyRow(
        state = listState,
        verticalAlignment = Alignment.CenterVertically,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    ) {
        items(months) { month ->
            MonthUi(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .fillParentMaxWidth()
                    .aspectRatio(1f),
                daysInMonth = month,
                headerTextSize = 50f,
                textSize = 40f,
                selectedDay = selectedDay,
                onDaySelected = onDaySelected
            )
        }
    }
}

@Composable
fun MonthUi(
    modifier: Modifier,
    daysInMonth: List<Day>,
    headerTextSize: Float,
    textSize: Float,
    titleColor: Color = Color.Blue,
    textColor: Color = Color.DarkGray,
    todayColor: Color = Color.Magenta,
    selectedDayColor: Color = Color.Blue,
    selectedDay: Day? = null,
    onDaySelected: (Day) -> Unit
) {
    val title = remember {
        val monthIndex = daysInMonth[15].month - 1
        DateUtil.getMonthNameByIndex(monthIndex) + " " + daysInMonth[15].year
    }

    val weekLettersMarginFromTitle = with(LocalDensity.current) { 48.dp.toPx() }

    val nativePaint = remember {
        TextPaint().apply {
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
    }

    var titleHeight by remember {
        mutableStateOf(0f)
    }

    var itemHeight by remember {
        mutableStateOf(0f)
    }

    var itemWidth by remember {
        mutableStateOf(0f)
    }

    val nativeRect = remember { Rect() }


    Canvas(
        modifier = modifier
            .pointerInput(true) {
                detectTapGestures { offset ->
                    var column = -1
                    var row = -1
                    val constant = titleHeight + weekLettersMarginFromTitle
                    val rowsCount = daysInMonth.size / COLUMNS_COUNT
                    var shouldBreak = false
                    for (i in 0 until COLUMNS_COUNT) {
                        if (shouldBreak) break
                        for (j in 0 until rowsCount) {
                            if (
                                (offset.x >= i * itemWidth && offset.x <= (i + 1) * itemWidth) &&
                                (offset.y >= j * itemHeight + constant && offset.y <= (j + 1) * itemHeight + constant)
                            ) {
                                row = j
                                column = i
                                shouldBreak = true
                                break
                            }
                        }
                    }

                    if (row != -1 && column != -1) {
                        val index = row * COLUMNS_COUNT + column
                        val day = daysInMonth[index]
                        if (day.isCurrentMonth) {
                            onDaySelected(day)
                        }
                    }

                }
            }
    ) {
        val width = this.size.width
        val height = this.size.height
        drawContext.canvas.nativeCanvas.apply {
            titleHeight =
                drawTitle(nativePaint, headerTextSize, title, nativeRect, titleColor, width)

            itemWidth = width / COLUMNS_COUNT

            drawWeekLetters(
                itemWidth,
                titleHeight,
                weekLettersMarginFromTitle,
                nativePaint,
                textSize,
                textColor
            )

            val rowsCount = daysInMonth.size / COLUMNS_COUNT
            itemHeight = (height - titleHeight - weekLettersMarginFromTitle) / rowsCount


            drawDays(
                daysInMonth,
                nativePaint,
                textSize,
                nativeRect,
                itemWidth,
                titleHeight,
                weekLettersMarginFromTitle,
                itemHeight,
                todayColor,
                textColor,
                selectedDay,
                selectedDayColor
            )

        }
    }

}

private fun NativeCanvas.drawDays(
    daysInMonth: List<Day>,
    nativePaint: TextPaint,
    textSize: Float,
    nativeRect: Rect,
    itemWidth: Float,
    titleHeight: Float,
    weekLettersMarginFromTitle: Float,
    itemHeight: Float,
    todayColor: Color,
    textColor: Color,
    selectedDay: Day?,
    selectedDayColor: Color
) {

    daysInMonth.forEachIndexed { index, day ->
        val dayText = day.day.toString()
        nativePaint.apply {
            this.style = Paint.Style.FILL
            this.textSize = textSize
            getTextBounds(dayText, 0, dayText.length, nativeRect)
        }
        val dayTextHeight = nativeRect.height()

        val x = (index % COLUMNS_COUNT) * itemWidth + itemWidth / 2
        val y = titleHeight + weekLettersMarginFromTitle +
                (index / COLUMNS_COUNT) * itemHeight + itemHeight - dayTextHeight / 2f

        if (selectedDay != null && index == daysInMonth.indexOf(selectedDay)) {
            drawCircle(
                x,
                y - dayTextHeight / 2f,
                itemHeight * 0.65f / 2,
                nativePaint.apply {
                    this.style = Paint.Style.STROKE
                    this.color = selectedDayColor.toArgb()
                    this.strokeWidth = 5f
                }
            )
        } else if (day.isToday) {
            drawCircle(
                x,
                y - dayTextHeight / 2f,
                itemHeight * 0.65f / 2,
                nativePaint.apply {
                    this.style = Paint.Style.FILL
                    this.color = todayColor.toArgb()
                }
            )
        }
        drawText(
            dayText,
            x,
            y,
            nativePaint.apply {
                this.style = Paint.Style.FILL
                this.color = if (day.isToday && day != selectedDay) Color.White.toArgb()
                else textColor.copy(alpha = if (day.isCurrentMonth) 1f else 0.5f).toArgb()
            }
        )
    }
}

private fun NativeCanvas.drawWeekLetters(
    itemWidth: Float,
    titleHeight: Float,
    weekLettersMarginFromTitle: Float,
    nativePaint: TextPaint,
    textSize: Float,
    textColor: Color
) {
    for (d in 0..6) {
        val weekLetter = DateUtil.getWeekLetterByIndex(d)

        val x = d * itemWidth + itemWidth / 2
        val y = titleHeight + weekLettersMarginFromTitle
        drawText(
            weekLetter,
            x,
            y,
            nativePaint.apply {
                this.style = Paint.Style.FILL
                this.textSize = textSize
                this.color = textColor.toArgb()
            }
        )
    }
}

private fun NativeCanvas.drawTitle(
    nativePaint: TextPaint,
    headerTextSize: Float,
    title: String,
    nativeRect: Rect,
    headerColor: Color,
    width: Float
): Float {
    nativePaint.apply {
        this.textSize = headerTextSize
        getTextBounds(title, 0, title.length, nativeRect)
        this.color = headerColor.toArgb()
        this.style = Paint.Style.FILL
    }
    val titleHeight = nativeRect.height().toFloat()
    drawText(
        title,
        width / 2f,
        titleHeight,
        nativePaint
    )
    return titleHeight
}

@Composable
private fun createMonthsData(): MutableList<List<Day>> {
    val months = remember {
        mutableListOf<List<Day>>().apply {

            val monthCount = 60
            calendar.timeInMillis = System.currentTimeMillis()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val currentMonth = calendar.get(Calendar.MONTH) + 1
            val currentYear = calendar.get(Calendar.YEAR)
            calendar.add(Calendar.MONTH, -monthCount / 2)

            repeat(monthCount) {
                val month = mutableListOf<Day>()

                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val previousCount = calendar.get(Calendar.DAY_OF_WEEK) - 1
                val nextCount = (42 - previousCount - daysInMonth) % 7

                calendar.add(Calendar.DAY_OF_MONTH, -previousCount)
                for (i in 1..previousCount) {
                    val day = Day(
                        day = calendar.get(Calendar.DAY_OF_MONTH),
                        month = calendar.get(Calendar.MONTH) + 1,
                        year = calendar.get(Calendar.YEAR),
                        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
                        isToday = false,
                        isCurrentMonth = false
                    )
                    month.add(day)
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                for (i in 1..daysInMonth) {
                    val m = calendar.get(Calendar.MONTH) + 1
                    val y = calendar.get(Calendar.YEAR)
                    val day = Day(
                        day = i,
                        month = m,
                        year = y,
                        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
                        isToday = currentDay == i && currentMonth == m && currentYear == y,
                        isCurrentMonth = true
                    )
                    month.add(day)
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
                for (i in 1..nextCount) {
                    val day = Day(
                        day = calendar.get(Calendar.DAY_OF_MONTH),
                        month = calendar.get(Calendar.MONTH) + 1,
                        year = calendar.get(Calendar.YEAR),
                        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
                        isToday = false,
                        isCurrentMonth = false
                    )
                    month.add(day)
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                this.add(month)
            }

        }
    }
    return months
}
