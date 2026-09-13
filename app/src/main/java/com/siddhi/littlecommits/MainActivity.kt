package com.siddhi.littlecommits

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LittleCommitsScreen(this)
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun LittleCommitsScreen(context: Context) {

    var messageText by remember {
        mutableStateOf("")
    }

    val messages = remember {
        mutableStateListOf<Message>()
    }

    val preferences = remember {
        context.getSharedPreferences(
            "little_commits",
            Context.MODE_PRIVATE
        )
    }

    val today = SimpleDateFormat(
        "MMMM dd, yyyy",
        Locale.getDefault()
    ).format(Date())

    // Load saved messages when the screen starts
    LaunchedEffect(Unit) {

        val savedMessages = preferences.getStringSet(
            "messages",
            emptySet()
        ) ?: emptySet()

        messages.clear()

        savedMessages.forEach { savedMessage ->

            val parts = savedMessage.split("|||")

            if (parts.size == 2) {

                messages.add(
                    Message(
                        text = parts[0],
                        date = parts[1]
                    )
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Text(
            text = "LITTLE COMMITS",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Text(
            text = today,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(
            value = messageText,
            onValueChange = {
                messageText = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Write something...")
            }
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Button(
            onClick = {

                if (messageText.isNotBlank()) {

                    val newMessage = Message(
                        text = messageText,
                        date = today
                    )

                    messages.add(newMessage)

                    // Save all messages
                    val savedMessages = messages.map {
                        "${it.text}|||${it.date}"
                    }.toSet()

                    preferences.edit()
                        .putStringSet(
                            "messages",
                            savedMessages
                        )
                        .apply()

                    messageText = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("COMMIT ♥")
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(
            text = "Today's commits: ${
                messages.count { it.date == today }
            }",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(messages) { message ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "♥ ${message.text}",
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