package com.siddhi.littlecommits.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
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
import com.google.firebase.firestore.Query
import com.siddhi.littlecommits.MainActivity
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LittleCommitsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {

        val snapshot = FirebaseFirestore
            .getInstance()
            .collection("messages")
            .orderBy(
                "timestamp",
                Query.Direction.ASCENDING
            )
            .get()
            .await()

        val messageCounts = mutableMapOf<String, Int>()

        snapshot.documents.forEach { document ->

            val date = document.getString("date")

            if (date != null) {
                messageCounts[date] =
                    (messageCounts[date] ?: 0) + 1
            }
        }

        provideContent {
            LittleCommitsWidgetContent(
                messageCounts = messageCounts
            )
        }
    }
}


@Composable
fun LittleCommitsWidgetContent(
    messageCounts: Map<String, Int>
) {

    val dateFormat = SimpleDateFormat(
        "MMMM dd, yyyy",
        Locale.getDefault()
    )

    val calendar = Calendar.getInstance()

    /*
     * Start 12 weeks ago.
     */
    calendar.add(
        Calendar.DAY_OF_YEAR,
        -(12 * 7 - 1)
    )

    /*
     * Create 12 weeks × 7 days.
     */
    val weeksData = List(12) {

        val week = List(7) {

            val date = dateFormat.format(
                calendar.time
            )

            val count =
                messageCounts[date] ?: 0

            calendar.add(
                Calendar.DAY_OF_YEAR,
                1
            )

            count
        }

        week
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    Color(0xFF000000)
                )
            )
            .clickable(
                actionStartActivity<MainActivity>()
            )
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Text(
            text = "LITTLE COMMITS",
            style = TextStyle(
                color = ColorProvider(
                    Color.White
                )
            )
        )

        Spacer(
            modifier = GlanceModifier.size(8.dp)
        )

        /*
         * Fixed square size for now.
         *
         * 12 columns × 12dp
         * + gaps
         * fits comfortably inside the widget.
         */
        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            weeksData.forEachIndexed { weekIndex, week ->

                Column(
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    week.forEachIndexed { dayIndex, count ->

                        PinkSquare(
                            count = count
                        )

                        if (dayIndex < 6) {

                            Spacer(
                                modifier =
                                    GlanceModifier.size(2.dp)
                            )
                        }
                    }
                }

                if (weekIndex < 11) {

                    Spacer(
                        modifier =
                            GlanceModifier.size(2.dp)
                    )
                }
            }
        }

        Spacer(
            modifier = GlanceModifier.size(8.dp)
        )

        Text(
            text = "every day, a little something ♥",
            style = TextStyle(
                color = ColorProvider(
                    Color(0xFFFF4F81)
                )
            )
        )
    }
}


@Composable
fun PinkSquare(
    count: Int
) {

    val color = when {

        count == 0 ->
            Color(0xFF1A1A1A)

        count == 1 ->
            Color(0xFFFFC1D1)

        count == 2 ->
            Color(0xFFFF7FA5)

        count == 3 ->
            Color(0xFFFF3F78)

        else ->
            Color(0xFFFF0054)
    }

    Box(
        modifier = GlanceModifier
            .size(12.dp)
            .background(
                ColorProvider(color)
            )
    ) {

        Text(
            text = " ",
            style = TextStyle(
                color = ColorProvider(color)
            )
        )
    }
}