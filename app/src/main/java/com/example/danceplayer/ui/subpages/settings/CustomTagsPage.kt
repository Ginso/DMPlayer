package com.example.danceplayer.ui.subpages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.danceplayer.ui.Fragment

@Composable
fun CustomTagsPage(onBack: () -> Unit) {
    Fragment("Custom Tags", onBack) {
        Text("Here you can define your custom tags", color = MaterialTheme.colorScheme.onBackground)
        HorizontalDivider()
        Text("Predefined Tags", style = MaterialTheme.typography.titleLarge)
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
        HorizontalDivider()
        Text("Custom Tags", style = MaterialTheme.typography.titleMedium)

    }

}

@Composable
fun RowScope.Cell(
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