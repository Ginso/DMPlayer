package com.example.danceplayer.ui.subpages.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.danceplayer.model.Song
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.util.MusicLibrary
import kotlinx.coroutines.launch
import kotlin.collections.emptyList


@Composable
fun ParseTagsPage(onBack: () -> Unit) {
    var tags = MusicLibrary.tags.map { it.name }
    var pattern = remember { mutableStateOf("") }
    var preview = remember { mutableStateOf(emptyList<PreviewItem>()) }
    var previewFailed = remember { mutableStateOf(emptyList<String>()) }
    val errorText = remember { mutableStateOf("") }
    val isLoading = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

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
            onValueChange = { 
                pattern.value = it 
                preview.value = emptyList<PreviewItem>()
                previewFailed.value = emptyList<String>()
            },
            label = { Text("Pattern") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { 
                coroutineScope.launch {
                    isLoading.value = true
                    val (previewItems, failedSongs) = previewTags(pattern.value, MusicLibrary.songs, tags, errorText, false)
                    preview.value = previewItems
                    previewFailed.value = failedSongs
                    isLoading.value = false
                }
            }) {
                Text("Preview")
            }
            Button(onClick = {
                coroutineScope.launch {
                    isLoading.value = true
                    val (previewItems, failedSongs) = previewTags(pattern.value, MusicLibrary.songs, tags, errorText, true)
                    MusicLibrary.save(context)
                    preview.value = previewItems
                    previewFailed.value = failedSongs
                    isLoading.value = false
                }
            }) {
                Text("Apply")
            }
        }

        if (previewFailed.value.isNotEmpty() || preview.value.isNotEmpty()) {
            Text("Parsed ${preview.value.size} songs successfully, failed to parse ${previewFailed.value.size} songs.")
            if (preview.value.isNotEmpty()) {
                Text("Parsed songs:")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    preview.value.forEach { item ->
                        Text("File: ${item.filePath}")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item.tags.forEach { (tag, value) ->
                                Text("$tag: $value")
                            }
                        }
                    }
                }
            }
            if (previewFailed.value.isNotEmpty()) {
                Text("Failed songs:")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    previewFailed.value.forEach { song ->
                        Text(song)
                    }
                }
            }
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
        if (errorText.value.isNotBlank()) {
            AlertDialog(
                onDismissRequest = { errorText.value = "" },
                title = { Text("Error") },
                text = { Text(errorText.value, modifier = Modifier.fillMaxWidth(), color = Color.Red) },
                confirmButton = {
                    Button(onClick = { errorText.value = "" }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

fun previewTags(pattern: String,
                songs: List<Song>,
                tags: List<String>,
                errorText: MutableState<String>,
                changeSongs: Boolean): Pair<List<PreviewItem>, List<String>> {
    val tagIndexList = tags.mapNotNull { tag ->
        val index = pattern.indexOf("<$tag>")
        if (index == -1) {
            null
        } else {
            Triple(tag, index, index + tag.length + 2)
        }
    }.sortedBy { it.second }
    val parts = ArrayList<String>()
    if(tagIndexList.isEmpty()) {
        errorText.value = "No valid tags found in pattern"
        return Pair(emptyList<PreviewItem>(), emptyList<String>())
    }
    var index = 0
    tagIndexList.forEach {
        var part = pattern.substring(index, it.second)
        if (part.isEmpty() && !parts.isEmpty()) {
            errorText.value = "Two tags cannot be directly adjacent without any separator to distinguish them"
            return Pair(emptyList<PreviewItem>(), emptyList<String>())
        }
        parts.add(part)
        parts.add(it.first)
        index = it.third
    }
    parts.add(pattern.substring(index, pattern.length))

    val folders = pattern.split("/").size
    val remainingSongs = ArrayList<String>()
    val result = songs.map { song ->
        var path = song.getPath()
        var remaining = path.split("/").takeLast(folders).joinToString("/")
        val map = HashMap<String, String>()
        parts.forEachIndexed { i, part ->
            if (i % 2 == 0) {
                if (remaining.startsWith(part)) {
                    remaining = remaining.substring(part.length)
                } else {
                    remainingSongs.add(path)
                    return@map null
                }
            } else {
                // code above ensures an uneven number of parts, so i + 1 is always valid
                val nextPart = parts[i + 1]

                val value = if (nextPart.isNotEmpty()) {
                    val index = remaining.indexOf(nextPart)
                    if (index == -1) {
                        remainingSongs.add(path)
                        return@map null
                    }
                    val result = remaining.substring(0, index)
                    remaining = remaining.substring(index)
                    result
                } else { // can only be last part, so take everything until the end of the string
                    val result = remaining
                    remaining = ""
                    result
                }
                map[part] = value
                if (changeSongs) {
                    song.tags[part] = value
                }
            }
        }
        PreviewItem(path, map)
    }
    return Pair(result.filterNotNull(), remainingSongs)
}

data class PreviewItem(
    val filePath: String,
    val tags: Map<String, String>
)