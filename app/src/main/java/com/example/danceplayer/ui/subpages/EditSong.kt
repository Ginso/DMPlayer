package com.example.danceplayer.ui.subpages

import androidx.compose.runtime.Composable
import com.example.danceplayer.model.Song
import com.example.danceplayer.ui.Fragment

class EditSong(
    val song: Song
) : Fragment() {

    override fun getTitle(): String {
        return "Edit Tags"
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current

        Main {
            val tags by MusicLibrary.tags

            for (tag in tags) {
                val value = song.getTagValue(tag)
                Row(
                    arrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(tag.name)
                    when (tag.type) {
                        Tag.Type.STRING -> {
                            var text by remember { mutableStateOf(value as? String ?: "") }
                            TextField(
                                value = text,
                                onValueChange = { newValue ->
                                    text = newValue
                                    song.tags[tag.name] = newValue
                                    MusicLibrary.save(context)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Tag.Type.INT -> {
                            var text by remember { mutableStateOf((value as? Int)?.toString() ?: "") }
                            TextField(
                                value = text,
                                onValueChange = { newValue ->
                                    text = newValue
                                    val intValue = newValue.toIntOrNull()
                                    if (intValue == null) {
                                        song.tags.remove(tag.name)
                                    } else {
                                        song.tags[tag.name] = intValue
                                    }
                                    MusicLibrary.save(context)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Tag.Type.FLOAT -> {
                            var text by remember { mutableStateOf((value as? Float)?.toString() ?: "") }
                            TextField(
                                value = text,
                                onValueChange = { newValue ->
                                    text = newValue
                                    val floatValue = newValue.toFloatOrNull()
                                    if (floatValue == null) {
                                        song.tags.remove(tag.name)
                                    } else {
                                        song.tags[tag.name] = floatValue
                                    }
                                    MusicLibrary.save(context)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                        Tag.Type.BOOL -> {
                            var checked by remember { mutableStateOf(value as? Boolean ?: false) }
                            Switch(
                                checked = checked,
                                onCheckedChange = { newValue ->
                                    checked = newValue
                                    song.tags[tag.name] = newValue
                                    MusicLibrary.save(context)
                                }
                            )
                        }
                        Tag.Type.DATETIME, Tag.Type.DATE, Tag.Type.TIME -> {
                            var text by remember { mutableStateOf((value as? String) ?: "") }
                            TextField(
                                value = text,
                                onValueChange = { newValue ->
                                    text = newValue
                                    val dateValue = DateTimeUtil.parse(newValue, tag.type)
                                    song.tags[tag.name] = dateValue
                                    MusicLibrary.save(context)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                            )
                        }

                        else -> {
                            Text(value.toString())
                        }
                    }
                }
            }
        }

    }
}