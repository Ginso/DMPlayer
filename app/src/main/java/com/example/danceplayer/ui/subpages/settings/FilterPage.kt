package com.example.danceplayer.ui.subpages.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import com.example.danceplayer.ui.theme.DefText
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.PreferenceUtil
import com.example.danceplayer.util.SimpleDropDown
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun FilterPage(onBack: () -> Unit) {
    var filterOptions = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().filterOptions) }
    val selectedRow = remember { mutableStateOf(-1) }
    
    Fragment("Edit you filter and sort options", onBack) {
        DefText("Here you can edit the filter and sort options for you will see when browsing your songs in the 'Dances' page.")
        DefText("Click on a line to edit, move or remove it.")
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
                    val tag = MusicLibrary.getAllTagsMap().get(tagName)
                    if(tag == null) {
                        DefText("INVALID")
                        continue
                    }
                    HeaderCell(o, tag, if(tag.type == Tag.Type.INT) 2 else "", "")
                }
            }
        }

        Button(onClick = {
            filterOptions.value.put(JSONArray().apply { // doesn't trigger re-render, but next line will
                put(JSONObject().apply {
                    put("filter", false)
                    put("tag", Song._RATING)
                    put("text", "Rating")
                })
            })
            selectedRow.value = filterOptions.value.length() - 1 // trigger re-render
        }) {
            Text("new Row")
        }

        Button(onClick = {
            // save to profile
            PreferenceUtil.getCurrentProfile().filterOptions = filterOptions.value
            PreferenceUtil.saveProfile()
            onBack()
        }) {
            Text("Save")
        }

        HorizontalDivider()

        if(selectedRow.value > -1 ) {
            val arr = filterOptions.value.getJSONArray(selectedRow.value)
            Column {
                for(j in 0 until arr.length()) {
                    val o = arr.getJSONObject(j)
                    val tagName = o.getString("tag")
                    val tag = MusicLibrary.getAllTagsMap().get(tagName)!!
                    val isFilter = o.getBoolean("filter")
                    val type = o.getJSONArray("type")
                    Column (
                        modifier = Modifier.padding(8.dp)
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(2.dp))
                    ) {
                        Row { // buttons
                            if( j > 0) {
                                Button(onClick = {
                                    val temp = arr.getJSONObject(j-1)
                                    arr.put(j-1, arr.getJSONObject(j))
                                    arr.put(j, temp)
                                    filterOptions.value = filterOptions.value // trigger re-render
                                }) {
                                    Text("↑")
                                }
                            }
                            if(j < arr.length() - 1) {
                                Button(onClick = {
                                    val temp = arr.getJSONObject(j+1)
                                    arr.put(j+1, arr.getJSONObject(j))
                                    arr.put(j, temp)
                                    filterOptions.value = filterOptions.value // trigger re-render
                                }) {
                                    Text("↓")
                                }
                            }
                            Button(onClick = {
                                arr.remove(j)
                                filterOptions.value = filterOptions.value // trigger re-render
                            }) {
                                Text("X")
                            }
                        }
                        Row { // filter/sorter
                            RadioButton(
                                selected = isFilter,
                                onClick = {
                                    o.put("filter", true)
                                    filterOptions.value = filterOptions.value // trigger re-render
                                }
                            )
                            DefText("Filter", modifier = Modifier.clickable {
                                o.put("filter", true)
                                filterOptions.value = filterOptions.value // trigger re-render
                            })
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(
                                selected = !isFilter,
                                onClick = {
                                    o.put("filter", false)
                                    filterOptions.value = filterOptions.value // trigger re-render
                                }
                            )
                            DefText("Sorter", modifier = Modifier.clickable {
                                o.put("filter", false)
                                filterOptions.value = filterOptions.value // trigger re-render
                            })
                        }
                        Row { // tag
                            DefText("Tag: ")
                            SimpleDropDown(
                                options = MusicLibrary.getAllTags().map { it.name },
                                selectedOption = tag.name,
                                onOptionSelected = { tn ->
                                    o.put("tag", tn)
                                    filterOptions.value = filterOptions.value // trigger re-render
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Row { // text
                            DefText("Text: ")
                            TextField(
                                value = o.optString("text", ""),
                                onValueChange = {
                                    o.put("text", it)
                                    filterOptions.value = filterOptions.value // trigger re-render
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Row { // text size
                            DefText("Text Size: ")
                            Button(onClick = {
                                val currentSize = o.optInt("textSize", 16)
                                o.put("textSize", if(currentSize <= 4) 4 else currentSize - 1)
                                filterOptions.value = filterOptions.value // trigger re-render
                            }) {
                                Text("-")
                            }
                            DefText("${o.optInt("textSize", 16)}")
                            Button(onClick = {
                                val currentSize = o.optInt("textSize", 16)
                                o.put("textSize", if(currentSize >= 72) 72 else currentSize + 1)
                                filterOptions.value = filterOptions.value // trigger re-render
                            }) {
                                Text("+")
                            }
                        }

                        if(isFilter) {
                            if(tag.type == Tag.Type.INT) {
                                SimpleDropDown(
                                    options = listOf("★★☆", "♫♫♫", "123", "Input"),
                                    selectedOption = type.getInt(0),
                                    onOptionSelected = { option ->
                                        if(option == type.getInt(0)) return@SimpleDropDown // no change
                                        o.put("type", listOf(option,0,5))
                                        filterOptions.value = filterOptions.value // trigger re-render
                                    },
                                )
                                if(type.getInt(0) < 3) { // input
                                    Row {
                                        DefText("Max Value:")
                                        TextField(
                                            value = "${type.getInt(2)}",
                                            onValueChange = {
                                                val intValue = it.toIntOrNull() ?: return@TextField
                                                o.put("type", listOf(type.getInt(0),type.getInt(1),intValue))
                                                filterOptions.value = filterOptions.value // trigger re-render
                                            },
                                            modifier = Modifier.width(50.dp),
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType = KeyboardType.Number
                                            )
                                        )
                                    }
                                }
                                val options = when(type.getInt(0)) {
                                    0 -> listOf(
                                            "tag ≤ _",
                                            "tag ≥ _",
                                        )
                                    1 -> listOf(
                                            "tag ≤ _",
                                            "tag ≥ _",
                                        )
                                    2 -> listOf(
                                            "tag ≤ _",
                                            "tag ≥ _",
                                            "tag < _",
                                            "tag > _",
                                        )

                                    else -> listOf(
                                            "tag ≤ _",
                                            "tag ≥ _",
                                            "tag < _",
                                            "tag > _",
                                            "_ < tag < _",
                                            "_ ≤ tag ≤ _",
                                            "_ ≤ tag < _",
                                            "_ < tag ≤ _"
                                        )
                                }
                                SimpleDropDown(
                                    options = options,
                                    selectedOption = type.getInt(1),
                                    onOptionSelected = { option ->
                                        if(option == type.getInt(1)) return@SimpleDropDown // no change
                                        o.put("type", listOf(type.getInt(0),option,type.getInt(2)))
                                        filterOptions.value = filterOptions.value // trigger re-render
                                    },
                                )
                            } else if(tag.type == Tag.Type.FLOAT || tag.type == Tag.Type.DATETIME) {
                                SimpleDropDown(
                                    options = listOf(
                                        "tag ≤ _",
                                        "tag ≥ _",
                                        "tag < _",
                                        "tag > _",
                                        "_ < tag < _",
                                        "_ ≤ tag ≤ _",
                                        "_ ≤ tag < _",
                                        "_ < tag ≤ _"
                                    ),
                                    selectedOption = type.getInt(0),
                                    onOptionSelected = { option ->
                                        if(option == type.getInt(0)) return@SimpleDropDown // no change
                                        o.put("type", listOf(option))
                                        filterOptions.value = filterOptions.value // trigger re-render
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun HeaderCell(o:JSONObject, tag: Tag, value: Any, value2: Any, onValueChange: (Any) -> Unit = {}, onValue2Change: (Any) -> Unit = {}) {
    val text = o.optString("text", "")
    val textSize = o.optInt("textSize", 16)
    Row(verticalAlignment = Alignment.CenterVertically) {
        if(o.getBoolean("filter")) {
            val typeArr = o.getJSONArray("type")
            val types = List(typeArr.length()) { typeArr.getInt(it) }
            if(tag.type == Tag.Type.INT) {
                val intVal = value as Int
                if(types[0] <= 2) {
                    if(types.size != 3) {
                        DefText("INVALID")
                        return
                    }
                    DefText("$text: ", )
                    for(k in 0 until types[2]) {
                        var filled = k <= intVal
                        if(types[0] == 2 && types[1] == 1) filled = k >= intVal
                        
                        if(types[0] == 0) DefText(if(filled) "★" else "☆", fontSize = textSize.sp)
                        else if(types[0] == 1) Text("♫", fontSize = textSize.sp, color = if(filled) MaterialTheme.colorScheme.onBackground else Color.Gray)
                        else if(types[0] == 2) Text("$k", fontSize = textSize.sp, color = if(filled) MaterialTheme.colorScheme.onBackground else Color.Gray)
                    }
                } else if(types[0] == 3) {
                    if(types.size != 3) {
                        DefText("INVALID")
                        return
                    }
                    if(types[1] > 3) {
                        // input
                        TextField (
                            value = "$value",
                            onValueChange = { onValueChange(it) },
                            modifier = Modifier.width(20.dp),
                            textStyle = TextStyle(fontSize = textSize.sp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                        DefText(if(types[1] == 4 || types[1] == 7) " < " else " ≤ ", fontSize = textSize.sp)
                    }
                    DefText("$text", fontSize = textSize.sp)
                    DefText(
                        if(types[1] == 3) " > "
                        else if(types[1] == 1) " ≥ "
                        else if(types[1] == 2 || types[1] == 4 ||types[1] == 6) " < " 
                        else " ≤ ",
                        fontSize = textSize.sp)
                    TextField(
                        value = "$value2",
                        onValueChange = { onValue2Change(it) },
                        modifier = Modifier.width(20.dp),
                        textStyle = TextStyle(fontSize = textSize.sp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                } else {
                    DefText("INVALID")
                }
            } else if(tag.type == Tag.Type.FLOAT) {
                if(types.size != 2) {
                    DefText("INVALID")
                    return
                }
                if(types[0] > 3) {
                    // input
                    TextField(
                        value = "$value",
                        onValueChange = { onValueChange(it) },
                        modifier = Modifier.width(20.dp),
                        textStyle = TextStyle(fontSize = textSize.sp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                    DefText(if(types[1] == 4 || types[1] == 7) " < " else " ≤ ", fontSize = textSize.sp)
                }
                DefText("$text", fontSize = textSize.sp)
                DefText(
                        if(types[0] == 3) " > "
                        else if(types[0] == 1) " ≥ "
                        else if(types[0] == 2 || types[0] == 4 ||types[0] == 6) " < " 
                        else " ≤ ",
                        fontSize = textSize.sp)
                TextField(
                    value = "$value2",
                    onValueChange = { onValue2Change(it) },
                    modifier = Modifier.width(20.dp),
                    textStyle = TextStyle(fontSize = textSize.sp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )
            } else if(tag.type == Tag.Type.BOOL) {
                DefText("$text: ", fontSize = textSize.sp)
                DefText(listOf("All", "Yes", "No")[value as Int], fontSize = textSize.sp)
            } else if(tag.type == Tag.Type.DATETIME) {
                if(types.size != 2) {
                    DefText("INVALID")
                    return
                }
                if(types[0] > 3) {
                    // input
                    TextField(
                        value = "$value",
                        onValueChange = { onValueChange(it) },
                        modifier = Modifier.width(40.dp),
                        textStyle = TextStyle(fontSize = textSize.sp),
                    )
                    DefText(if(types[1] == 4 || types[1] == 7) " < " else " ≤ ", fontSize = textSize.sp)
                }
                DefText("$text", fontSize = textSize.sp)
                DefText(
                        if(types[0] == 3) " > "
                        else if(types[0] == 1) " ≥ "
                        else if(types[0] == 2 || types[0] == 4 ||types[0] == 6) " < " 
                        else " ≤ ",
                        fontSize = textSize.sp)
                TextField(
                    value = "$value2",
                    onValueChange = { onValue2Change(it) },
                    modifier = Modifier.width(40.dp),
                    textStyle = TextStyle(fontSize = textSize.sp),
                )
            } else if(tag.type == Tag.Type.STRING) {
                DefText("$text: ", fontSize = textSize.sp)
                TextField(
                    value = "$value",
                    onValueChange = { onValueChange(it) },
                    textStyle = TextStyle(fontSize = textSize.sp),
                    modifier = Modifier.width(100.dp),
                )
            } else {
                DefText("Unknown Type", fontSize = textSize.sp)
            }
            
        } else { // sorter
            Text(o.optString("text", ""), fontSize = textSize.sp)
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

fun getDefaultFilterOptions(): JSONArray {
    return JSONArray().apply {
        put(JSONArray().apply {
            put(JSONObject().apply {
                put("filter", true)
                put("tag", Song._RATING)
                put("type", listOf(0,5,0))
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