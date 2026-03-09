package com.example.danceplayer.ui.subpages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import com.example.danceplayer.model.Song
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.PreferenceUtil
import com.example.danceplayer.util.SimpleDropDown
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun ItemLayoutsPage(onBack: () -> Unit) {
    val currentType = remember { mutableIntStateOf(0) }
    val layoutBrowser = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().itemLayoutBrowser) }
    val layoutPlaylists = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().itemLayoutPlaylists) }
    val layoutQueue = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().itemLayoutQueue) }
    val layoutQueueParty = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().itemLayoutQueueParty) }
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

    Fragment("Song Item Layouts", onBack) {
        Text("Here you can customize the layout of the items in the dance browser, playlists and queue. You can choose which tags are shown and how they are arranged.")
        HorizontalDivider()
        SimpleDropDown(listOf("Dance Browser", "Playlists", "Player Queue", "Player Queue Party"),
            currentType.intValue,
            {n -> currentType.intValue=n})
        if(currentType.intValue == 3) {
            Text("The Queue Party layout is used for the current song when you enable the Queue Party mode in the queue view. For example you can use it to show the current dance big enough to read from a distance.")
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
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
                    .border(1.dp, MaterialTheme.colorScheme.outline)

            ) {
                val index = if(currentPath.value.isEmpty()) -1 else currentPath.value.last()
                Column(
                    modifier = Modifier
                        .padding(all = 8.dp)
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
                                Text("X")
                            }
                        }
                    }
                    if (currentObject.getInt("type") == ElementType.TAG) {

                        Row {
                            Text("Tag: ")
                            val tags = MusicLibrary.getAllTags().map { it.name } + Song._PLAYING_AFTER
                            SimpleDropDown(
                                options = tags,
                                selectedOption = currentObject.getString("tag"),
                                onOptionSelected = { tag ->
                                    currentObject.put("tag", tag)
                                    currentPath.value = currentPath.value
                                }
                            )
                        }

                        // TODO

                    } else {
                        Row(horizontalArrangement = Arrangement.SpaceEvenly) { // add Buttons
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
                                    put("type", ElementType.ROW)
                                    put("items", JSONArray())
                                }
                                currentObject.getJSONArray("items").put(index, newItem)
                                currentPath.value = currentPath.value + index
                            }) {
                                Text("+ Container")
                            }
                        }

                        // TODO
                    }
                }
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


object ElementType {
    const val ROW = 0
    const val COLUMN = 1
    const val TAG = 2
}
object DisplayType {
    const val DEFAULT = 0
    const val STARS = 1
    const val NOTES = 2
}