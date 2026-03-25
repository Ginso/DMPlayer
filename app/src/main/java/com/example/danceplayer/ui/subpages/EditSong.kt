package com.example.danceplayer.ui.subpages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.example.danceplayer.model.Song
import com.example.danceplayer.model.Tag
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.MyTextField

class EditSong(
    val song: Song
) : Fragment() {

    override fun getTitle(): String {
        return "Edit Tags"
    }

    override fun sameType(other: Fragment): Boolean {
        return other is EditSong && other.song == song
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current

        Main {
            val tags by MusicLibrary.tags
            var maxSize by remember { mutableStateOf(IntSize.Zero) }
            val density = LocalDensity.current
            val widthDp = with(density) {
                maxSize.width.toDp()
            }

            Text(song.getPath())

            for (tag in tags) {
                val value = song.getTagValue(tag)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    var modifier: Modifier = Modifier
                    if(maxSize.width != 0) modifier = modifier.width(widthDp)
                    Text(tag.name,
                        modifier = modifier
                            .onGloballyPositioned {
                                if(it.size.width > maxSize.width)
                                    maxSize = it.size
                            }
                        )
                    when (tag.type) {
                        Tag.Type.STRING -> {
                            var text by remember { mutableStateOf(value as? String ?: "") }
                            MyTextField(
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
                            MyTextField(
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
                            MyTextField(
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
                            MyTextField(
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