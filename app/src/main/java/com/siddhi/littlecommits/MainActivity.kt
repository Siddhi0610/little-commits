package com.siddhi.littlecommits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.siddhi.littlecommits.widget.LittleCommitsWidget
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LittleCommitsApp()
        }
    }
}

@androidx.compose.runtime.Composable
fun LittleCommitsApp() {

    val db = remember {
        FirebaseFirestore.getInstance()
    }

    val context = LocalContext.current

    val messages = remember {
        mutableStateListOf<Message>()
    }

    var messageText by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    /*
     * Listen for changes in Firestore.
     */
    LaunchedEffect(Unit) {

        val listener = db.collection("messages")
            .orderBy(
                "timestamp",
                Query.Direction.DESCENDING
            )
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                val loadedMessages =
                    snapshot?.documents?.mapNotNull { document ->

                        val text = document.getString("text")
                        val date = document.getString("date")

                        if (text != null && date != null) {
                            Message(
                                text = text,
                                date = date
                            )
                        } else {
                            null
                        }
                    } ?: emptyList()

                messages.clear()
                messages.addAll(loadedMessages)
            }

        try {
            awaitCancellation()
        } finally {
            listener.remove()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Text(
                text = "Little Commits",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Every little message becomes a commit.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            /*
             * Contribution graph
             */
            CommitGrid(
                messages = messages
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            /*
             * Message input
             */
            OutlinedTextField(
                value = messageText,
                onValueChange = {
                    messageText = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Write a little commit")
                },
                placeholder = {
                    Text("Something you want him to see...")
                }
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            /*
             * Commit button
             */
            Button(
                onClick = {

                    if (messageText.isBlank()) {
                        return@Button
                    }

                    val today = SimpleDateFormat(
                        "MMMM dd, yyyy",
                        Locale.getDefault()
                    ).format(Date())

                    val newMessage = hashMapOf(
                        "text" to messageText,
                        "date" to today,
                        "timestamp" to System.currentTimeMillis()
                    )

                    scope.launch {

                        try {

                            /*
                             * Save message to Firebase.
                             */
                            db.collection("messages")
                                .add(newMessage)
                                .await()

                            /*
                             * Clear input.
                             */
                            messageText = ""

                            /*
                             * Update all Little Commits widgets
                             * on this device.
                             */
                            LittleCommitsWidget()
                                .updateAll(context)

                        } catch (e: Exception) {

                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {

                Text("Commit")
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Recent commits",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            /*
             * Display messages.
             */
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {

                items(messages) { message ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {

                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = message.date,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}