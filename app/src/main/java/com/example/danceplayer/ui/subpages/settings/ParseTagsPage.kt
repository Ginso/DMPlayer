package com.example.danceplayer.ui.subpages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AssistChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.util.MusicLibrary


@Composable
fun ParseTagsPage(onBack: () -> Unit) {
    var tags = MusicLibrary.tags.map { it.name }
    var pattern = remember { mutableStateOf("") }

    Fragment("Fill Tags from File name and path", onBack) {
        Text("Here you can automatically fill the tags of your songs based on their file name and path. This is especially useful if you have a well-structured music library where the file names and paths contain relevant information about the songs (e.g., Dance/Artist - Title.mp3).")
        Text("Enter the pattern of your file names and paths. Use / for folder separation.")
        Text("Examples:")
        Text("<Dance>/<Artist> - <Title>.mp3 -> fills the dance with the name of the innermost folder, the artist and title from the file name")
        Text("<Title> (<TPM>TPM).mp3 -> Fills title and TPM if the files are named like 'Great Song (30TPM).mp3'")
        Text("You can use the following tags in your pattern:")
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tags.forEach { tag ->
                AssistChip(
                    onClick = { pattern.value += "<$tag>" },
                    label = { Text(tag) },
                    enabled = false
                )
            }
        }

        OutlinedTextField(
            value = pattern.value,
            onValueChange = { pattern.value = it },
            label = { Text("Pattern") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}