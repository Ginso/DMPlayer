package com.example.danceplayer.ui.subpages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CustomTagsPage(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Custom Tags", color = MaterialTheme.colorScheme.onBackground) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Hier können benutzerdefinierte Tags verwaltet werden", color = MaterialTheme.colorScheme.onBackground)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Predefined Tags", style = MaterialTheme.typography.titleMedium)
                Column {
                    Row {
                        Cell("duration", 1f)
                        Cell("Date/Time", 1f)
                    }
                    Row {
                        Cell("playing_after", 1f)
                        Cell("Date/Time", 1f)
                    }
                    Row {
                        Cell("last_modified", 1f)
                        Cell("Date/Time", 1f)
                    }
                    Row {
                        Cell("title", 1f)
                        Cell("Text", 1f)
                    }
                    Row {
                        Cell("artist", 1f)
                        Cell("Text", 1f)
                    }
                    Row {
                        Cell("album", 1f)
                        Cell("Text", 1f)
                    }
                    Row {
                        Cell("dance", 1f)
                        Cell("Text", 1f)
                    }
                    Row {
                        Cell("year", 1f)
                        Cell("Number", 1f)
                    }
                }
                Text("TODO These tags are...", color = MaterialTheme.colorScheme.onBackground)
                Divider()
                Text("Custom Tags", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun Cell(
    text: String,
    weight: Float
) {
    Box(
        modifier = Modifier
            .weight(weight)
            .border(1.dp, MaterialTheme.colorScheme.onBackground)
            .padding(8.dp)
    ) {
        Text(text, color = MaterialTheme.colorScheme.onBackground)
    }
}