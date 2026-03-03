package com.example.danceplayer.ui.subpages.settings

@Composable
fun FilterPage(onBack: () -> Unit) {
    var filterOptions = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().filterOptions) }
    val selectedRow = remember { mutableStateOf(-1) }
    
    Fragment("Edit you filter and sort options", onBack) {
        DefText("Here you can edit the filter and sort options for you will see when browsing your songs in the 'Dances' page.")
        DefText("Click on a line to edit or remove it.")
        HorizontalDivider()
        if(!validateJSON(filterOptions.value)) {
            DefText("Error loading filter options")
            Button(onClick = {filterOptions.value = getDefaultFilterOptions() }) {
                Text("Reset to default")
            }
            return@Fragment
        }

        for(i in 0 until filterOptions.value.length()) {
            val row = filterOptions.value.getJSONArray(i)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedRow.value = i
                    }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for(j in 0 until row.length()) {
                    val o = row.getJSONObject(j)
                    val tagName = o.getString("tag")
                    val tag = MusicLibrary.tagMap.get(tagName)
                    if(tag == null) {
                        DefText("INVALID")
                        continue
                    }
                    HeaderCell(o, tag, if(tag.type == Tag.Type.INT) 2 else "", "")
                }
            }
        }

    }

}

@Composable
fun HeaderCell(o:JsonObject, tag: Tag, value: Any, value2: Any, onValueChange: (Any) -> Unit = {}, onValue2Change: (Any) -> Unit = {}) {
   val text = o.optString("text", "")
    Row(verticalAlignment = Alignment.CenterVertically) {
        if(o.getBoolean("filter")) {
            val typeArr = o.getJSONArray("type")
            val types = List(typeArr.length()) { typeArr.getInt(it) }
            if(tag.type == Tag.Type.INT) {
                if(types[0] <= 2) {
                    if(types.size != 3) {
                        DefText("INVALID")
                        continue
                    }
                    DefText("$text: ")
                    for(k in 0 until types[1]) {
                        var filled = k <= value
                        if(types[0] == 2 && types[2] == 1) filled = k >= value
                        if(types[0] == 2 && types[2] == 2) filled = k == value
                        if(types[0] == 0) DefText(if(filled) "★" else "☆")
                        else if(types[0] == 1) DefText("♫", color = if(filled) MaterialTheme.colorScheme.onBackground else Color.Gray)
                        else if(types[0] == 2) DefText("$k", color = if(filled) MaterialTheme.colorScheme.onBackground else Color.Gray)
                    }
                } else if(types[0] == 3) {
                    if(types.size != 3) {
                        DefText("INVALID")
                        continue
                    }
                    if(types[1] > 0) {
                        // input
                        TextField(
                            value = "$value",
                            onValueChange = { onValueChange(it) },
                            modifier = Modifier.width(20.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                        DefText(if(types[1] == 1) " < " else " ≤ ")
                    }
                    DefText("$text")
                    if(types[2] > 0) {
                        // input
                        DefText(if(types[2] == 1) " < " else " ≤ ")
                        TextField(
                            value = "$value2",
                            onValueChange = { onValue2Change(it) },
                            modifier = Modifier.width(20.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }

                } else {
                    DefText("INVALID")
                }
            } else if(tag.type == Tag.Type.FLOAT) {
                if(types.size != 2) {
                    DefText("INVALID")
                    continue
                }
                if(types[0] > 0) {
                    // input
                    TextField(
                        value = "$value",
                        onValueChange = { onValueChange(it) },
                        modifier = Modifier.width(20.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                    DefText(if(types[0] == 1) " < " else " ≤ ")
                }
                DefText("$text")
                if(types[1] > 0) {
                    // input
                    DefText(if(types[1] == 1) " < " else " ≤ ")
                    TextField(
                        value = "$value2",
                        onValueChange = { onValue2Change(it) },
                        modifier = Modifier.width(20.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                }
            } else if(tag.type == Tag.Type.BOOL) {
                DefText("$text: ")
                DefText(listOf("All", "Yes", "No")[value as Int])
            } else if(tag.type == Tag.Type.DATETIME) {
                if(types.size != 2) {
                    DefText("INVALID")
                    continue
                }
                if(types[0] > 0) {
                    // input
                    TextField(
                        value = "$value",
                        onValueChange = { onValueChange(it) },
                        modifier = Modifier.width(40.dp),
                    )
                    DefText(if(types[0] == 1) " < " else " ≤ ")
                }
                DefText("$text")
                if(types[1] > 0) {
                    // input
                    DefText(if(types[1] == 1) " < " else " ≤ ")
                    TextField(
                        value = "$value2",
                        onValueChange = { onValue2Change(it) },
                        modifier = Modifier.width(40.dp),
                    )
                }
            } else if(tag.type == Tag.Type.STRING) {
                DefText("$text: ")
                TextField(
                    value = "$value",
                    onValueChange = { onValueChange(it) },
                    modifier = Modifier.width(100.dp),
                )
            } else {
                DefText("Unknown Type")
            }
            
        } else { // sorter
            Text(o.optString("text", ""))
        }
    }
}

fun validateJSON(jsonArray: JSONArray): Boolean {
    return try {
        for (i in 0 until jsonArray.length()) {
            val row = jsonArray.optJSONArray(i)
            if(row == null) {
                return false
            }
            for (j in 0 until row.length()) {
                val o = row.getJSONObject(j)
                if (!o.has("filter") || !o.has("tag")) {
                    return false
                }
                if (o.getBoolean("filter")) {
                    val arr = o.optJSONArray("type")
                    if (arr == null) {
                        return false
                    }
                    // arr may only have (positive) int values
                    for (k in 0 until arr.length()) {
                        if (arr.optInt(k, -1) == -1) {
                            return false
                        }
                    }
                }
            }
        }
        true
    } catch (_: Exception) {
        false
    }
}

fun getDefaultFilterOptions() {
    return JSONArray().apply {
        put(JSONArray().apply {
            put(JSONObject().apply {
                put("filter", true)
                put("tag", Song._RATING)
                put("type", listof(0,5,0))
                put("text", "Rating")
            })
        })
        put(JSONArray().apply {
            put(JSONObject().apply {
                put("filter", false)
                put("tag", Song._DURATION)
                put("text", "Duration")
            })
            put(JSONObject().apply {
                put("filter", false)
                put("tag", Song._RATING)
                put("text", "Rating")
            })
        })
    }
}

/*
    Integer:
    0: Stars (amount, [at least, at most, exact])
    1: Notes (as Stars)
    2: Numbers (as Stars)
    3: inputs (2x [-, <, <=])

 */