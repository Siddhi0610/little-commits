package com.siddhi.littlecommits.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LittleCommitsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {

        val db = FirebaseFirestore.getInstance()

        val messageDates = try {

            db.collection("messages")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    document.getString("date")
                }

        } catch (e: Exception) {

            emptyList()
        }

        provideContent {

            WidgetContent(
                messageDates = messageDates
            )
        }
    }
}

@androidx.compose.runtime.Composable
fun WidgetContent(
    messageDates: List<String>
) {

    val dateFormat = SimpleDateFormat(
        "MMMM dd, yyyy",
        Locale.getDefault()
    )

    val calendar = Calendar.getInstance()

    calendar.set(
        Calendar.DAY_OF_WEEK,
        Calendar.SUNDAY
    )

    calendar.add(
        Calendar.WEEK_OF_YEAR,
        -11
    )

    val weeks = mutableListOf<List<Int>>()

    repeat(12) {

        val week = mutableListOf<Int>()

        repeat(7) {

            val date = dateFormat.format(
                calendar.time
            )

            val count = messageDates.count {
                it == date
            }

            week.add(count)

            calendar.add(
                Calendar.DAY_OF_YEAR,
                1
            )
        }

        weeks.add(week)
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(Color.White)
            )
            .padding(12.dp),

        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "LITTLE COMMITS",

            style = TextStyle(
                color = ColorProvider(Color.Black)
            )
        )

        Spacer(
            modifier = GlanceModifier.size(10.dp)
        )

        Row {

            weeks.forEach { week ->

                Column {

                    week.forEach { count ->

                        WidgetCommitSquare(
                            count = count
                        )

                        Spacer(
                            modifier = GlanceModifier.size(4.dp)
                        )
                    }
                }

                Spacer(
                    modifier = GlanceModifier.size(4.dp)
                )
            }
        }

        Spacer(
            modifier = GlanceModifier.size(10.dp)
        )

        Text(
            text = "Less        More",

            style = TextStyle(
                color = ColorProvider(Color.DarkGray)
            )
        )
    }
}

@androidx.compose.runtime.Composable
fun WidgetCommitSquare(
    count: Int
) {

    val color = when {

        count == 0 ->
            Color(0xFFF5F5F5)

        count == 1 ->
            Color(0xFFFCE4EC)

        count == 2 ->
            Color(0xFFF8BBD0)

        count == 3 ->
            Color(0xFFF06292)

        else ->
            Color(0xFFE91E63)
    }

    Box(
        modifier = GlanceModifier
            .size(16.dp)
            .background(
                ColorProvider(color)
            )
    ) {
        // Empty content.
        // The Box itself provides the colored square.
    }
}