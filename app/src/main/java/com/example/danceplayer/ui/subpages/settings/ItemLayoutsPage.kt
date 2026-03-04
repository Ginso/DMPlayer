package com.example.danceplayer.ui.subpages.settings

@Composable
fun ItemLayoutsPage(onBack: () -> Unit) {
    var currentType = remember { mutableStateOf(0) }
    var layoutBrowser = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().itemLayoutBrowser) }
    var layoutPlaylists = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().itemLayoutPlaylists) }
    var layoutQueue = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().itemLayoutQueue) }
    var layoutQueueParty = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().itemLayoutQueueParty) }
    var currentLayout = when(currentType.value) {
        0 -> layoutBrowser
        1 -> layoutPlaylists
        2 -> layoutQueue
        3 -> layoutQueueParty
        else -> layoutBrowser
    }
    var currentPath = remember { mutableStateOf<List<Int>>(emptyList()) }
    var currentObject: JSONObject? = null
    var parentObject: JSONObject? = null
    currentObject = currentLayout.value
    for(index in currentPath.value) {
        parentObject = currentObject
        currentObject = currentObject?.getJSONArray("items")?.getJSONObject(index)
    }

    Fragment("Song Item Layouts", onBack) {
        DefText("Here you can customize the layout of the items in the dance browser, playlists and queue. You can choose which tags are shown and how they are arranged.")
        HorizontalDivider()
        Row {
            ItemLayoutTypeButton("Dance Browser", 0, currentType)
            ItemLayoutTypeButton("Playlists", 1, currentType)
            ItemLayoutTypeButton("Queue", 2, currentType)
            ItemLayoutTypeButton("QueueParty", 3, currentType)
        }
        if(currentType.value == 3) {
            DefText("The Queue Party layout is used for the current song when you enable the Queue Party mode in the queue view. For example you can use it to show the current dance big enough to read from a distance.")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            LayoutTree(currentLayout.value, currentPath)
            VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp).fillMaxHeight())
            if(currentObject != null) {
                Column {
                    if(currentPath.size > 0) { // not root object}
                        Row { // Buttons
                            val index = currentPath.value.last()
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
                        if (currentObject.getInt("type") == ElementType.TAG.type) {
                            
                        } else {
                            Row { // add Buttons 
                                Button(onClick = {
                                    val newItem = JSONObject().apply {
                                        put("type", ElementType.TAG)
                                        put("tag", Song._RATING)
                                    }
                                    parentObject.getJSONArray("items").put(index, newItem)
                                    currentPath.value = currentPath.value + index
                                }) {
                                    Text("+ Tag")
                                }
                                Button(onClick = {
                                    val newItem = JSONObject().apply {
                                        put("type", ElementType.ROW)
                                        put("items", JSONArray())
                                    }
                                    parentObject.getJSONArray("items").put(index, newItem)
                                    currentPath.value = currentPath.value + index
                                }) {
                                    Text("+ Container")
                                }
                            }
                        }

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
    if(type == ElementType.TAG.type) {
        val tag = layout.getString("tag")
        DefText(tag, 
            modifier = Modifier
                .background(if(isSelected) Color.Yellow else Color.Transparent)
                .clickable { selectedPath.value = path })
    } else {
        DefText(if(type == ElementType.ROW.type) "Row" else "Column")
        val items = layout.getJSONArray("items")
        Column(modifier = Modifier
            .padding(start = 16.dp)
            .background(if(isSelected) Color.Yellow else Color.Transparent)
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
    Row {
        RadioButton(
            selected = currentType.value == type,
            onClick = { currentType.value = type }
        )
        Text(text, modifier = Modifier.padding(start = 8.dp).clickable { currentType.value = type })
    }
}

fun getDefaultLayout(type: Int): JSONArray {
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



enum class LayoutCategory(val type: Int) {
    BROWSER(0),
    PLAYLISTS(1),
    QUEUE(2),
    QUEUE_PARTY(3)
}
enum class ElementType(val type: Int) {
    ROW(0),
    COLUMN(1),
    TAG(2)
}
enum class DisplayType(val type: Int) {
    DEFAULT(0)
    STARS(1),
    NOTES(2),
}