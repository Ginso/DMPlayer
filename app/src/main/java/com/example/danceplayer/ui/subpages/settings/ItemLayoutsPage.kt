package com.example.danceplayer.ui.subpages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.danceplayer.model.Song
import com.example.danceplayer.model.Tag
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.MyTextField
import com.example.danceplayer.util.PreferenceUtil
import com.example.danceplayer.util.SimpleDropDown
import com.example.danceplayer.util.SongItem
import org.json.JSONArray
import org.json.JSONObject

class ItemLayoutsPage : Fragment() {

    override fun getTitle(): String {
        return "Song Item Layouts"
    }

    @Composable
    override fun Content(onBack: () -> Unit) {
        val profile = PreferenceUtil.getCurrentProfile()
        val currentType = remember { mutableIntStateOf(0) }
        val copyType = remember { mutableIntStateOf(0) }
        val layoutBrowser = remember { mutableStateOf(profile.itemLayoutBrowser) }
        val layoutPlaylists = remember { mutableStateOf(profile.itemLayoutPlaylists) }
        val layoutQueue = remember { mutableStateOf(profile.itemLayoutQueue) }
        val layoutQueueParty = remember { mutableStateOf(profile.itemLayoutQueueParty) }
        val sample = remember { mutableStateOf(MusicLibrary.songs.value.shuffled().take(10)) }

        val changed = listOf(
            profile.itemLayoutBrowser.toString() != layoutBrowser.value.toString(),
            profile.itemLayoutPlaylists.toString() != layoutPlaylists.value.toString(),
            profile.itemLayoutQueue.toString() != layoutQueue.value.toString(),
            profile.itemLayoutQueueParty.toString() != layoutQueueParty.value.toString(),
        )

        val currentLayout = when(currentType.intValue) {
            0 -> layoutBrowser
            1 -> layoutPlaylists
            2 -> layoutQueue
            3 -> layoutQueueParty
            else -> layoutBrowser
        }
        val currentPath = remember { mutableStateOf<List<Int>>(emptyList()) }
        var currentObject: JSONObject = currentLayout.value
        var parentObject: JSONObject? = null
        for(index in currentPath.value) {
            parentObject = currentObject as JSONObject?
            currentObject = currentObject.getJSONArray("items").getJSONObject(index)
        }

        Main {
            Text("Here you can customize the layout of the items in the dance browser, playlists and queue. You can choose which tags are shown and how they are arranged.")
            HorizontalDivider()
            SimpleDropDown(listOf("Dance Browser", "Playlists", "Player Queue", "Player Queue Party"),
                currentType.intValue,
                {n -> currentType.intValue=n},
                Modifier.fillMaxWidth())
            if(currentType.intValue == 3) {
                Text("The Queue Party layout is used for the current song when you enable the Queue Party mode in the queue view. For example you can use it to show the current dance big enough to read from a distance.")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Copy From")
                SimpleDropDown(listOf("Dance Browser", "Playlists", "Player Queue", "Player Queue Party"),
                    copyType.intValue,
                    {n -> copyType.intValue=n},
                    Modifier.padding(start=8.dp).weight(1f)
                )
                Button(
                    onClick = {
                        val origin = when(copyType.intValue) {
                            0 -> layoutBrowser
                            1 -> layoutPlaylists
                            2 -> layoutQueue
                            3 -> layoutQueueParty
                            else -> layoutBrowser
                        }
                        currentLayout.value = JSONObject(origin.toString())
                    },
                    enabled = currentType.intValue != copyType.intValue
                ) {
                    Text("Copy")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        val profile = PreferenceUtil.getCurrentProfile()
                        when(currentType.intValue) {
                            0 -> profile.itemLayoutBrowser = currentLayout.value
                            1 -> profile.itemLayoutPlaylists = currentLayout.value
                            2 -> profile.itemLayoutQueue = currentLayout.value
                            else -> profile.itemLayoutQueueParty = currentLayout.value
                        }
                        PreferenceUtil.saveProfile()
                    },
                    enabled = changed[currentType.intValue]
                ) {
                    Text("Save")
                }

                Button(
                    onClick = {
                        val profile = PreferenceUtil.getCurrentProfile()
                        profile.itemLayoutBrowser = layoutBrowser.value
                        profile.itemLayoutPlaylists = layoutPlaylists.value
                        profile.itemLayoutQueue = layoutQueue.value
                        profile.itemLayoutQueueParty = layoutQueueParty.value
                        PreferenceUtil.saveProfile()
                    },
                    enabled = changed.any { it }
                ) {
                    Text("Save all")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .border(1.dp, MaterialTheme.colorScheme.outline)

                ) {

                    Column(
                        modifier = Modifier
                            .padding(all = 8.dp)
                    ) {
                        LayoutTree(currentLayout.value, currentPath)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(1.dp, MaterialTheme.colorScheme.outline)

                ) {
                    val index = if(currentPath.value.isEmpty()) -1 else currentPath.value.last()
                    val type = currentObject.getInt("type")
                    Column(
                        modifier = Modifier
                            .padding(all = 8.dp),
                        Arrangement.spacedBy(4.dp)
                    ) {
                        if(parentObject != null) { // not root object
                            Row { // Buttons
                                if(index > 0) {
                                    Button(onClick = {
                                        parentObject.getJSONArray("items").apply {
                                            val temp = getJSONObject(index - 1)
                                            put(index - 1, getJSONObject(index))
                                            put(index, temp)
                                        }
                                        currentPath.value = currentPath.value.dropLast(1) + (index - 1)
                                    }) {
                                        Text("↑")
                                    }
                                }
                                if(index < parentObject.getJSONArray("items").length() - 1) {
                                    Button(onClick = {
                                        parentObject.getJSONArray("items").apply {
                                            val temp = getJSONObject(index + 1)
                                            put(index + 1, getJSONObject(index))
                                            put(index, temp)
                                        }
                                        currentPath.value = currentPath.value.dropLast(1) + (index + 1)
                                    }) {
                                        Text("↓")
                                    }
                                }
                                Button(onClick = {
                                    parentObject.getJSONArray("items").remove(index)
                                    currentPath.value = currentPath.value.dropLast(1)
                                }) {
                                    Text("✖")
                                }
                            }
                        }
                        if (type == ElementType.TAG) {
                            val tagName = currentObject.getString("tag")
                            val tag = MusicLibrary.allTagsMap.value[tagName]
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Tag: ")
                                val tags = MusicLibrary.allTags.value.map { it.name } + Song._PLAYING_AFTER
                                SimpleDropDown(
                                    options = tags,
                                    selectedOption = tagName,
                                    onOptionSelected = { tag ->
                                        currentObject.put("tag", tag)
                                        currentLayout.value = JSONObject(currentLayout.value.toString())
                                    },
                                    Modifier.weight(1f)
                                )
                            }
                            if(tag == null) {
                                Text("INVALID TAG")
                                return@Box
                            }
                            
                            if(tag.type == Tag.Type.BOOL) {
                                Row { // trueText
                                    Text("Text when true")
                                    MyTextField(
                                        value = currentObject.optString("trueText", ""),
                                        modifier = Modifier.weight(1f),
                                        onValueChange = { trueText ->
                                            currentObject.put("trueText", trueText)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        }
                                    )
                                } // trueText
                                Row { // falseText
                                    Text("Text when false")
                                    MyTextField(
                                        value = currentObject.optString("falseText", ""),
                                        modifier = Modifier.weight(1f),
                                        onValueChange = { falseText ->
                                            currentObject.put("falseText", falseText)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        }
                                    )
                                } // falseText
                            } else {
                                Row { // prefix
                                    Text("Text before: ")
                                    MyTextField(
                                        value = currentObject.optString("prefix", ""),
                                        modifier = Modifier.weight(1f),
                                        onValueChange = { prefix ->
                                            currentObject.put("prefix", prefix)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        }
                                    )
                                } // prefix
                                Row { // suffix
                                    Text("Text after: ")
                                    MyTextField(
                                        value = currentObject.optString("suffix", ""),
                                        modifier = Modifier.weight(1f),
                                        onValueChange = { suffix ->
                                            currentObject.put("suffix", suffix)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        }
                                    )
                                } // suffix
                            }

                            if(tag.type == Tag.Type.INT) {
                                Row(verticalAlignment = Alignment.CenterVertically) { // display
                                    Text("Display type: ")
                                    SimpleDropDown(
                                        options = listOf("★★☆", "♫♫♫", "123"),
                                        selectedOption = currentObject.optInt("display", 2),
                                        onOptionSelected = { option ->
                                            currentObject.put("display", option)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        },
                                        modifier = Modifier.weight(1f)
                                    )

                                } // display

                                if(currentObject.optInt("display", 2) < 2) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Max Value: ")
                                        MyTextField(
                                            value = "${currentObject.optInt("maxValue", 5)}",
                                            onValueChange = {
                                                val intValue = it.toIntOrNull() ?: return@MyTextField
                                                currentObject.put("maxValue", intValue)
                                                currentLayout.value = JSONObject(currentLayout.value.toString())
                                            },
                                            modifier = Modifier
                                                .width(40.dp),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number
                                            ),


                                        )
                                    }
                                }
                            }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) { // textSize
                                Text("Text size: ")
                                Text("-",
                                    style = TextStyle(fontSize = 32.sp),
                                    modifier = Modifier
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                        .clickable {
                                            val currentSize = currentObject.optInt("textSize", 16)
                                            currentObject.put("textSize", if(currentSize <= 4) 4 else currentSize - 1)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        }
                                )
                                Text("${currentObject.optInt("textSize", 16)}",
                                    style = TextStyle(fontSize = 20.sp))
                                Text("+",
                                    style = TextStyle(fontSize = 26.sp),
                                    modifier = Modifier
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                        .clickable {
                                            val currentSize = currentObject.optInt("textSize", 16)
                                            currentObject.put("textSize", if(currentSize >= 72) 72 else currentSize + 1)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        }
                                )
                            } // textSize

                            if(tag.type != Tag.Type.INT || currentObject.optInt("display", 2) != 1) {
                                Row(verticalAlignment = Alignment.CenterVertically) { // gray
                                    Switch(
                                        checked = currentObject.optBoolean("gray", false),
                                        onCheckedChange = { isChecked ->
                                            currentObject.put("gray", isChecked)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        }
                                    )
                                    Text("Gray", Modifier.padding(start=8.dp))
                                } // gray
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) { // bold
                                Switch(
                                    checked = currentObject.optBoolean("bold", false),
                                    onCheckedChange = { isChecked ->
                                        currentObject.put("bold", isChecked)
                                        currentLayout.value = JSONObject(currentLayout.value.toString())
                                    }
                                )
                                Text("Bold", Modifier.padding(start=8.dp))
                            } // bold

                            Row(verticalAlignment = Alignment.CenterVertically) { // italic
                                Switch(
                                    checked = currentObject.optBoolean("italic", false),
                                    onCheckedChange = { isChecked ->
                                        currentObject.put("italic", isChecked)
                                        currentLayout.value = JSONObject(currentLayout.value.toString())
                                    }
                                )
                                Text("Italic", Modifier.padding(start=8.dp))
                            } // italic

                            Row(verticalAlignment = Alignment.CenterVertically) { // underline
                                Switch(
                                    checked = currentObject.optBoolean("underline", false),
                                    onCheckedChange = { isChecked ->
                                        currentObject.put("underline", isChecked)
                                        currentLayout.value = JSONObject(currentLayout.value.toString())
                                    }
                                )
                                Text("Underline", Modifier.padding(start=8.dp))
                            } // underline
                        } else if(type == ElementType.SPACE) {
                            val size = currentObject.getInt("size")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = size == 0,
                                    onCheckedChange = { isChecked ->
                                        currentObject.put("size", if(isChecked) 0 else 16)
                                        currentLayout.value = JSONObject(currentLayout.value.toString())
                                    }
                                )
                                Text("Fill remaining space", Modifier.padding(start=8.dp))
                            }
                            if(size != 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Size: ")
                                    Text("-",
                                        style = TextStyle(fontSize = 32.sp),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp, horizontal = 8.dp)
                                            .clickable {
                                                val currentSize = currentObject.optInt("size", 16)
                                                currentObject.put("size", if(currentSize <= 4) 4 else currentSize - 1)
                                                currentLayout.value = JSONObject(currentLayout.value.toString())
                                            }
                                    )
                                    Text("${currentObject.optInt("size", 16)}",
                                        style = TextStyle(fontSize = 20.sp))
                                    Text("+",
                                        style = TextStyle(fontSize = 26.sp),
                                        modifier = Modifier
                                            .padding(vertical = 4.dp, horizontal = 8.dp)
                                            .clickable {
                                                val currentSize = currentObject.optInt("size", 16)
                                                currentObject.put("size", if(currentSize >= 99) 99 else currentSize + 1)
                                                currentLayout.value = JSONObject(currentLayout.value.toString())
                                            }
                                    )
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Direction: ")
                                SimpleDropDown(
                                    options = listOf("Row", "Column"),
                                    selectedOption = currentObject.getInt("type"),
                                    onOptionSelected = { t ->
                                        currentObject.put("type", t)
                                        currentLayout.value = JSONObject(currentLayout.value.toString())
                                    },
                                    Modifier.weight(1f)
                                )
                            }

                            if(parentObject != null) {

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Width: ")
                                    SimpleDropDown(
                                        options = listOf("Min", "Max"),
                                        selectedOption = currentObject.optInt("width", 0),
                                        onOptionSelected = { t ->
                                            currentObject.put("width", t)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        },
                                        Modifier.weight(1f)
                                    )
                                }

                                

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Height: ")
                                    SimpleDropDown(
                                        options = listOf("Min", "Max"),
                                        selectedOption = currentObject.optInt("height", 0),
                                        onOptionSelected = { t ->
                                            currentObject.put("height", t)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        },
                                        Modifier.weight(1f)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Item alignment: ")
                                SimpleDropDown(
                                    options = if(type == ElementType.ROW) listOf( "Top", "Center", "Bottom") else listOf("Left", "Center", "Right"),
                                    selectedOption = currentObject.optInt("alignment", 1 - type),
                                    onOptionSelected = { t ->
                                        currentObject.put("alignment", t)
                                        currentLayout.value = JSONObject(currentLayout.value.toString())
                                    },
                                    Modifier.weight(1f)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) { // textSize
                                Text("Space: ")
                                Text("-",
                                    style = TextStyle(fontSize = 32.sp),
                                    modifier = Modifier
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                        .clickable {
                                            val currentSize = currentObject.optInt("space", 8)
                                            currentObject.put("space", if(currentSize <= 0) 0 else currentSize - 1)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        }
                                )
                                Text("${currentObject.optInt("space", 8)}",
                                    style = TextStyle(fontSize = 20.sp))
                                Text("+",
                                    style = TextStyle(fontSize = 26.sp),
                                    modifier = Modifier
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                        .clickable {
                                            val currentSize = currentObject.optInt("space", 8)
                                            currentObject.put("space", if(currentSize >= 99) 99 else currentSize + 1)
                                            currentLayout.value = JSONObject(currentLayout.value.toString())
                                        }
                                )
                            }

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) { // add Buttons
                                Button(onClick = {
                                    val newItem = JSONObject().apply {
                                        put("type", ElementType.TAG)
                                        put("tag", Song._RATING)
                                    }
                                    currentObject.getJSONArray("items").put(index, newItem)
                                    currentPath.value = currentPath.value + index
                                }) {
                                    Text("+ Tag")
                                }
                                Button(onClick = {
                                    val newItem = JSONObject().apply {
                                        put("type", ElementType.SPACE)
                                        put("size", 0)
                                    }
                                    currentObject.getJSONArray("items").put(index, newItem)
                                    currentPath.value = currentPath.value + index
                                }) {
                                    Text("+ Space")
                                }
                                Button(onClick = {
                                    val newItem = JSONObject().apply {
                                        put("type", ElementType.ROW)
                                        put("items", JSONArray())
                                    }
                                    currentObject.getJSONArray("items").put(index, newItem)
                                    currentPath.value = currentPath.value + index
                                }) {
                                    Text("+ Container")
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            //preview
            Row {
                Text("Preview:", style = MaterialTheme.typography.titleLarge)
                Text("↻", modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clickable {
                        sample.value = MusicLibrary.songs.value.shuffled().take(10)
                    }
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for(song in sample.value) {
                    SongItem(song, currentLayout.value, Modifier.fillMaxWidth())
                }
            }
        }
    }

    @Composable
    fun LayoutTree(layout: JSONObject, selectedPath: MutableState<List<Int>>, path: List<Int> = emptyList()) {
        val isSelected = selectedPath.value == path
        val type = layout.getInt("type")
        if(type == ElementType.TAG) {
            val tag = layout.getString("tag")
            Text(tag, 
                modifier = Modifier
                    .background(if (isSelected) Color.Yellow else Color.Transparent)
                    .clickable { selectedPath.value = path })
        } else {
            Text(if(type == ElementType.ROW) "Row" else "Column",
                modifier = Modifier
                    .background(if (isSelected) Color.Yellow else Color.Transparent)
                    .clickable { selectedPath.value = path })
            val items = layout.getJSONArray("items")
            Column(modifier = Modifier
                .padding(start = 16.dp)
                .clickable { selectedPath.value = path }
            ) {
                for(i in 0 until items.length()) {
                    LayoutTree(items.getJSONObject(i), selectedPath, path + i)
                }
            }
        }
    }

    @Composable
    fun ItemLayoutTypeButton(text: String, type: Int, currentType: MutableState<Int>) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = currentType.value == type,
                onClick = { currentType.value = type }
            )
            Text(text, modifier = Modifier
                .padding(start = 8.dp)
                .clickable { currentType.value = type })
        }
    }

    fun getDefaultLayout(type: Int): JSONObject {
        return JSONObject().apply {
            put("type", ElementType.ROW)
            put("items", JSONArray().apply {
                if(type > 0) {
                    put(JSONObject().apply {
                        put("type", ElementType.TAG)
                        put("tag", Song._POSITION)
                        put("gray", true)
                    })

                    put(JSONObject().apply {
                        put("type", ElementType.COLUMN)
                        put("items", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", ElementType.TAG)
                                put("tag", Song._DURATION)
                            })
                            put(JSONObject().apply {
                                put("type", ElementType.TAG)
                                put("tag", Song._PLAYING_AFTER)
                            })
                        })
                    })
                } else {
                    put(JSONObject().apply {
                        put("type", ElementType.TAG)
                        put("tag", Song._DURATION)
                    })
                }




                put(JSONObject().apply {
                    put("type", ElementType.COLUMN)
                    put("items", JSONArray().apply {
                        if(type > 0) {
                            put(JSONObject().apply {
                                put("type", ElementType.TAG)
                                put("tag", Song._DANCE)
                                if(type == 3) {
                                    put("size", 20)
                                }
                            })
                        }
                        put(JSONObject().apply {
                            put("type", ElementType.TAG)
                            put("tag", Song._TPM)
                            put("decimal", 1)
                            put("suffix", " TPM")
                        })
                        if(type < 2) {
                            put(JSONObject().apply {
                                put("type", ElementType.TAG)
                                put("tag", Song._RATING)
                                put("display", DisplayType.STARS)
                            })
                        }
                    })
                })

                put(JSONObject().apply {
                    put("type", ElementType.COLUMN)
                    put("items", JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", ElementType.TAG)
                            put("tag", Song._TITLE)
                        })
                        put(JSONObject().apply {
                            put("type", ElementType.TAG)
                            put("tag", Song._ARTIST)
                            put("gray", true)
                        })
                    })
                })
            })
        }
    }
}



object ElementType {
    const val ROW = 0
    const val COLUMN = 1
    const val TAG = 2
    const val SPACE = 3
}
object DisplayType {
    const val DEFAULT = 0
    const val STARS = 1
    const val NOTES = 2
}