package com.example.danceplayer.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DancesPage() {
    val selectedDance = remember { mutableStateOf<String?>(null) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        var dances = MusicLibrary.songs.map { it.getDance() }
        val countPerDance = dances.groupingBy { it }.eachCount()
        dances = dances.distinct().sorted()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            for (dance in dances) {
                val count = countPerDance[dance] ?: 0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .clickable { selectedDance.value = dance }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text(
                            text = dance,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$count Songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF9500) // #FF9500
                        )
                    }
                }
            }
        }
    }

    if (selectedDance.value != null) {
        DanceSongsPage(dance = selectedDance.value!!, onBack = { selectedDance.value = null })
    }
}
