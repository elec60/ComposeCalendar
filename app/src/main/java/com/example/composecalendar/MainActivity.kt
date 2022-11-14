package com.example.composecalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.composecalendar.ui.theme.ComposeCalendarTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sdf = SimpleDateFormat("dd/MM/yyy", Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        setContent {
            ComposeCalendarTheme {

                var selectedDay by remember {
                    mutableStateOf<Day?>(null)
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CalendarUi(
                        selectedDay = selectedDay,
                        onDaySelected = {
                            selectedDay = it
                        }
                    )
                    selectedDay?.let {
                        calendar.set(Calendar.DAY_OF_MONTH, it.day)
                        calendar.set(Calendar.MONTH, it.month - 1)
                        calendar.set(Calendar.YEAR, it.year)
                        Text(text = sdf.format(calendar.time), fontSize = 24.sp)
                    }
                }

            }
        }
    }
}
