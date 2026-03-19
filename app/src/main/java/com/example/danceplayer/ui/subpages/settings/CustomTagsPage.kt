package com.example.danceplayer.ui.subpages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.danceplayer.model.Song
import com.example.danceplayer.model.Tag
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.PreferenceUtil
import kotlinx.coroutines.launch

@Composable
fun CustomTagsPage(onBack: () -> Unit) {

    val deleteTag = remember { mutableStateOf<Tag?>(null) }
    val deleteValues = remember { mutableStateOf(true) }
    val isLoading = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Fragment("Custom Tags", onBack) {
        Text("Here you can define your custom tags.")
        Text("Important: They have nothing to do with the tags stored in the file itself like mp3-tags. " +
                "They are stored seperately by this app in a file." +
                "You can however fill them automatically from filenames and/or folder names")
        HorizontalDivider()
        Text("Generated Tags", style = MaterialTheme.typography.titleLarge)
        Text("The value of these tags are generated and can be displayed, but not changed")
        Column {
            Row {
                Cell("duration", 1f)
                Cell("Date/Time", 1f)
            }
            Row {
                Cell("playing_after", 1f)
                Cell("Date/Time", 1f)
            }
        }
        Text("Predefined Tags", style = MaterialTheme.typography.titleLarge)
        Text("These Tags are available per default.")
        Column {
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
        HorizontalDivider()
        Text("Custom Tags", style = MaterialTheme.typography.titleMedium)
        Text("Here you can define your own tags")
        Column {
            val tags = MusicLibrary.tags.value
            for(tag in tags) {
                if(tag.name in listOf(Song._TITLE, Song._ARTIST, Song._DANCE, Song._TPM)) continue
                Row {
                    Cell(tag.name, 1f)
                    Cell(tag.type.getText(), 1f)
                    Box(
                        modifier = Modifier
                            .border(1.dp, MaterialTheme.colorScheme.onBackground)
                            .padding(8.dp)
                    ) {
                        Button(onClick = {
                            deleteTag.value = tag
                        }) {
                            Text("✖")
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier.padding(8.dp).border(1.dp, MaterialTheme.colorScheme.onBackground)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text("New Tag", style = MaterialTheme.typography.titleMedium)
                Row {
                    Text("Name: ")
                }
            }
        }
    }

    if(deleteTag.value != null) {
        AlertDialog(
            onDismissRequest = { deleteTag.value = null },
            title = { Text("Delete Tag: " + deleteTag.value) },
            text = {
                Row {
                    Switch(checked = deleteValues.value, onCheckedChange = { v -> deleteValues.value=v})
                    Text("Delete values for all songs")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading.value = true
                            MusicLibrary.tags.value = MusicLibrary.tags.value - deleteTag.value!!
                            if(deleteValues.value) {
                                for(song in MusicLibrary.allSongs.value) {
                                    song.tags.remove(deleteTag.value!!.name)
                                }
                                MusicLibrary.allSongs.value = MusicLibrary.allSongs.value.toList()
                            }
                            MusicLibrary.save(context)
                            deleteTag.value = null
                            isLoading.value = false
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        deleteTag.value = null
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )

    }

    if (isLoading.value) {
        AlertDialog(
            onDismissRequest = { /* do nothing */ },
            title = { Text("Loading...") },
            text = {
                Text(
                    "Please wait...",
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = { /* no buttons */ }
        )
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
        Text(text)
    }
}