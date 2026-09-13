package com.siddhi.littlecommits


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CommitGrid(messages: List<Message>) {

    val dateFormat = SimpleDateFormat(
        "MMMM dd, yyyy",
        Locale.getDefault()
    )

    val calendar = Calendar.getInstance()

    // Move to the beginning of the current week.
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

    // Go back 12 weeks.
    calendar.add(Calendar.WEEK_OF_YEAR, -11)

    val weeks = mutableListOf<List<Pair<String, Int>>>()

    repeat(12) {

        val week = mutableListOf<Pair<String, Int>>()

        repeat(7) {

            val date = dateFormat.format(calendar.time)

            val count = messages.count {
                it.date == date
            }

            week.add(date to count)

            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        weeks.add(week)
    }

    Column {

        Text(
            text = "COMMITS",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            weeks.forEach { week ->

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    week.forEach { (_, count) ->

                        CommitSquare(
                            count = count
                        )
                    }
                }
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = "Less",
                style = MaterialTheme.typography.bodySmall
            )

            CommitSquare(count = 0)
            CommitSquare(count = 1)
            CommitSquare(count = 2)
            CommitSquare(count = 3)
            CommitSquare(count = 4)

            Text(
                text = "More",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun CommitSquare(count: Int) {

    val pink = when {
        count == 0 -> Color(0xFFF5F5F5)
        count == 1 -> Color(0xFFFCE4EC)
        count == 2 -> Color(0xFFF8BBD0)
        count == 3 -> Color(0xFFF06292)
        else -> Color(0xFFE91E63)
    }

    Spacer(
        modifier = Modifier
            .size(14.dp)
            .background(pink)
    )
}