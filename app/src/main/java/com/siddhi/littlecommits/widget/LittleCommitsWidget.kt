package com.siddhi.littlecommits.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.siddhi.littlecommits.MainActivity
import kotlinx.coroutines.tasks.await
import java.util.Calendar


class LittleCommitsWidget : GlanceAppWidget() {

    /*
     * Use the actual size of the widget.
     */
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {

        /*
         * Read all messages from Firestore.
         */
        val snapshot = FirebaseFirestore
            .getInstance()
            .collection("messages")
            .get()
            .await()

        /*
         * Count messages for each calendar day.
         */
        val messageCounts =
            mutableMapOf<String, Int>()

        snapshot.documents.forEach { document ->

            /*
             * Your MainActivity stores this:
             *
             * "date" -> "September 15, 2026"
             */
            val dateString =
                document.getString("date")

            if (dateString != null) {

                messageCounts[dateString] =
                    (messageCounts[dateString] ?: 0) + 1
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
     * Actual dimensions supplied by the launcher.
     */
    val widgetWidth =
        LocalSize.current.width

    val widgetHeight =
        LocalSize.current.height

    /*
     * GitHub-style graph.
     *
     * 18 columns = roughly 18 weeks
     * 7 rows    = 7 days
     */
    val weeks = 18
    val days = 7

    /*
     * Tiny spacing between squares.
     */
    val gap = 2.dp

    /*
     * Small outer margin.
     */
    val horizontalPadding = 4.dp
    val verticalPadding = 5.dp

    /*
     * Calculate square size from WIDTH.
     *
     * This is the important part.
     *
     * 18 columns must collectively occupy
     * almost the entire widget width.
     */
    val squareFromWidth =
        (
                widgetWidth
                        - horizontalPadding * 2
                        - gap * (weeks - 1)
                ) / weeks

    /*
     * Calculate square size from HEIGHT.
     *
     * 7 rows must fit vertically too.
     */
    val squareFromHeight =
        (
                widgetHeight
                        - verticalPadding * 2
                        - gap * (days - 1)
                ) / days

    /*
     * Use the smaller dimension so the
     * complete graph always fits.
     */
    val squareSize =
        minOf(
            squareFromWidth,
            squareFromHeight
        )

    /*
     * Build the dates for the last
     * 18 weeks.
     */
    val calendar =
        Calendar.getInstance()

    calendar.add(
        Calendar.DAY_OF_YEAR,
        -(weeks * days - 1)
    )

    /*
     * IMPORTANT:
     *
     * This is exactly the same date format
     * used by MainActivity when saving messages.
     */
    val dateFormat =
        java.text.SimpleDateFormat(
            "MMMM dd, yyyy",
            java.util.Locale.getDefault()
        )

    /*
     * Create:
     *
     * 18 columns
     * ×
     * 7 rows
     */
    val weeksData =
        List(weeks) {

            List(days) {

                val date =
                    dateFormat.format(
                        calendar.time
                    )

                /*
                 * Get REAL message count.
                 */
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
     * Tapping it opens MainActivity.
     */
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(
                ColorProvider(
                    Color.Black
                )
            )
            .clickable(
                actionStartActivity<MainActivity>()
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        /*
         * Left padding.
         */
        Spacer(
            modifier =
                GlanceModifier.size(
                    horizontalPadding
                )
        )

        /*
         * CONTRIBUTION GRAPH
         */
        weeksData.forEachIndexed { weekIndex, week ->

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                week.forEachIndexed { dayIndex, count ->

                    PinkSquare(
                        count = count,
                        size = squareSize
                    )

                    /*
                     * Vertical spacing.
                     */
                    if (dayIndex < days - 1) {

                        Spacer(
                            modifier =
                                GlanceModifier.size(gap)
                        )
                    }
                }
            }

            /*
             * Horizontal spacing.
             */
            if (weekIndex < weeks - 1) {

                Spacer(
                    modifier =
                        GlanceModifier.size(gap)
                )
            }
        }

        /*
         * Right padding.
         */
        Spacer(
            modifier =
                GlanceModifier.size(
                    horizontalPadding
                )
        )
    }
}


@Composable
fun PinkSquare(
    count: Int,
    size: Dp
) {

    /*
     * Contribution intensity.
     */
    val color = when {

        /*
         * 0 messages
         */
        count == 0 ->
            Color(0xFF202020)

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

    Box(
        modifier = GlanceModifier
            .size(size)
            .background(
                ColorProvider(color)
            )
    ) {

        /*
         * Keeps the Box as an actual rendered
         * widget element.
         */
        Text(
            text = " ",
            style = TextStyle(
                color = ColorProvider(color)
            )
        )
    }
}