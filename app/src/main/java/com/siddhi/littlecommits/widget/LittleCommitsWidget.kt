package com.siddhi.littlecommits.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.google.firebase.firestore.FirebaseFirestore
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

        /*
         * Read the real messages from Firestore.
         */
        val snapshot = FirebaseFirestore
            .getInstance()
            .collection("messages")
            .get()
            .await()

        /*
         * Count messages for each date.
         */
        val messageCounts =
            mutableMapOf<String, Int>()

        snapshot.documents.forEach { document ->

            val date =
                document.getString("date")

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

    /*
     * 18 weeks across.
     * 7 days vertically.
     */
    val weeks = 18
    val days = 7

    /*
     * Start 18 weeks ago.
     */
    val calendar =
        Calendar.getInstance()

    calendar.add(
        Calendar.DAY_OF_YEAR,
        -(weeks * days - 1)
    )

    /*
     * EXACT same format used by MainActivity.
     */
    val dateFormat =
        SimpleDateFormat(
            "MMMM dd, yyyy",
            Locale.getDefault()
        )

    /*
     * Build the graph data.
     *
     * graph[week][day] = number of messages
     */
    val graph =
        List(weeks) {

            List(days) {

                val date =
                    dateFormat.format(
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
        }

    /*
     * Entire widget.
     *
     * No title.
     * No footer.
     * Black background.
     * Entire widget is clickable.
     */
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    Color.Black
                )
            )
            .clickable(
                actionStartActivity<MainActivity>()
            )
    ) {

        /*
         * Seven rows.
         *
         * defaultWeight() works HERE because
         * this Row is a child of Column.
         *
         * Each row receives equal height.
         */
        for (day in 0 until days) {

            Row(
                modifier =
                    GlanceModifier
                        .defaultWeight()
                        .fillMaxWidth(),

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                /*
                 * Eighteen squares across.
                 *
                 * IMPORTANT:
                 *
                 * defaultWeight() is used HERE,
                 * inside RowScope.
                 *
                 * This is the part I got wrong
                 * before.
                 */
                for (week in 0 until weeks) {

                    ContributionSquare(
                        count = graph[week][day],

                        modifier =
                            GlanceModifier
                                .defaultWeight()
                                .fillMaxHeight()
                    )
                }
            }
        }
    }
}


@Composable
fun ContributionSquare(
    count: Int,
    modifier: GlanceModifier
) {

    /*
     * Black → pink contribution intensity.
     */
    val squareColor =
        when {

            /*
             * 0 messages
             */
            count == 0 ->
                Color(0xFF181818)

            /*
             * 1 message
             */
            count == 1 ->
                Color(0xFFFFC7D6)

            /*
             * 2 messages
             */
            count == 2 ->
                Color(0xFFFF8EAE)

            /*
             * 3 messages
             */
            count == 3 ->
                Color(0xFFFF4F81)

            /*
             * 4+ messages
             */
            else ->
                Color(0xFFFF0054)
        }

    /*
     * The modifier containing defaultWeight()
     * comes from the RowScope at the call site.
     */
    Box(
        modifier =
            modifier
                .background(
                    ColorProvider(
                        squareColor
                    )
                )
    ) {

        /*
         * Real content inside the Box.
         */
        Text(
            text = " ",
            style =
                TextStyle(
                    color =
                        ColorProvider(
                            squareColor
                        )
                )
        )
    }
}