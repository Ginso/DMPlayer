package com.example.danceplayer.ui.subpages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
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
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun FilterPage(onBack: () -> Unit) {
    var filterOptions = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().filterOptions) }
    val selectedRow = remember { mutableStateOf(-1) }
    
    Fragment("Edit you filter and sort options", onBack) {
        Text("Here you can edit the filter and sort options for you will see when browsing your songs in the 'Dances' page.")
        Text("Click on a line to edit, move or remove it.")
        HorizontalDivider()
        if(!validateJSON(filterOptions.value)) {
            Text("Error loading filter options")
            Button(onClick = {filterOptions.value = getDefaultFilterOptions() }) {
                Text("Reset to default")
            }
            return@Fragment
        }
        Column(
            modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(4.dp))
        ) {
            for(i in 0 until filterOptions.value.length()) {
                val row = filterOptions.value.getJSONArray(i)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedRow.value = i
                        }
                        .padding(4.dp)
                        // todo min height
                        .background(if(selectedRow.value == i) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent),

                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for(j in 0 until row.length()) {
                        val o = row.getJSONObject(j)
                        val tagName = o.getString("tag")
                        val tag = MusicLibrary.allTagsMap.value[tagName]
                        if(tag == null) {
                            Text("INVALID")
                            continue
                        }
                        HeaderCell(o, tag, if(tag.type == Tag.Type.INT) 2 else "", "")
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
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
        }

        HorizontalDivider()

        if(selectedRow.value > -1 ) {
            val arr = filterOptions.value.getJSONArray(selectedRow.value)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                for(j in 0 until arr.length()) {
                    val o = arr.getJSONObject(j)
                    val tagName = o.getString("tag")
                    val tag = MusicLibrary.allTagsMap.value[tagName]!!
                    val isFilter = o.getBoolean("filter")
                    val type = o.optJSONArray("type") ?: JSONArray()
                    Box(
                        modifier = Modifier.padding(8.dp)
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.onBackground, shape = RoundedCornerShape(2.dp)),
                    ) {
                        Column (
                            modifier = Modifier.padding(8.dp)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp)) { // buttons
                                if( j > 0) {
                                    Text("↑",
                                        style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold),
                                        modifier = Modifier
                                            .clickable {
                                                val temp = arr.getJSONObject(j-1)
                                                arr.put(j-1, arr.getJSONObject(j))
                                                arr.put(j, temp)
                                                filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render

                                            }
                                    )
                                }
                                if(j < arr.length() - 1) {
                                    Text("↓",
                                        style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.ExtraBold),
                                        modifier = Modifier
                                            .clickable {
                                                val temp = arr.getJSONObject(j+1)
                                                arr.put(j+1, arr.getJSONObject(j))
                                                arr.put(j, temp)
                                                filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                            }
                                    )
                                }

                                Text("✖",
                                    style = TextStyle(fontSize = 26.sp),
                                    modifier = Modifier
                                        .clickable {
                                            arr.remove(j)
                                            filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render

                                        }
                                )
                            } // buttons
                            Row(verticalAlignment = Alignment.CenterVertically) { // filter/sorter
                                RadioButton(
                                    selected = isFilter,
                                    onClick = {
                                        o.put("filter", true)
                                        filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                    }
                                )
                                Text("Filter", modifier = Modifier.clickable {
                                    o.put("filter", true)
                                    filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                })
                                Spacer(modifier = Modifier.width(16.dp))
                                RadioButton(
                                    selected = !isFilter,
                                    onClick = {
                                        o.put("filter", false)
                                        filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                    }
                                )
                                Text("Sorter", modifier = Modifier.clickable {
                                    o.put("filter", false)
                                    filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                })
                            } // filter/sorter
                            Row(verticalAlignment = Alignment.CenterVertically) { // text
                                Text("Text: ")
                                MyTextField(
                                    value = o.optString("text", ""),
                                    onValueChange = {
                                        o.put("text", it)
                                        filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } // text
                            Row(verticalAlignment = Alignment.CenterVertically) { // tag
                                Text("Tag: ")
                                SimpleDropDown(
                                    options = MusicLibrary.allTags.value.map { it.name },
                                    selectedOption = tag.name,
                                    onOptionSelected = { tn ->
                                        o.put("tag", tn)
                                        filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                    },
                                    Modifier.weight(1f)
                                )
                            } // tag
                            Row(verticalAlignment = Alignment.CenterVertically) { // text size
                                Text("Text Size: ")
                                Text("-",
                                    style = TextStyle(fontSize = 32.sp),
                                    modifier = Modifier
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                        .clickable {
                                            val currentSize = o.optInt("textSize", 16)
                                            o.put("textSize", if(currentSize <= 4) 4 else currentSize - 1)
                                            filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                        }
                                )
                                Text("${o.optInt("textSize", 16)}",
                                    style = TextStyle(fontSize = 20.sp))
                                Text("+",
                                    style = TextStyle(fontSize = 26.sp),
                                    modifier = Modifier
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                        .clickable {
                                            val currentSize = o.optInt("textSize", 16)
                                            o.put("textSize", if(currentSize >= 72) 72 else currentSize + 1)
                                            filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                        }
                                )
                            } // text size

                            if(isFilter) {
                                when(tag.type) {
                                    Tag.Type.INT -> {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SimpleDropDown(
                                                options = listOf("★★☆", "♫♫♫", "123", "Input"),
                                                selectedOption = type.getInt(0),
                                                onOptionSelected = { option ->
                                                    if(option == type.getInt(0)) return@SimpleDropDown // no change
                                                    o.put("type", listOf(option,0,5))
                                                    filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                            if(type.getInt(0) < 3) { // input
                                                    Text("Max Value:")
                                                    MyTextField(
                                                        value = "${type.getInt(2)}",
                                                        onValueChange = {
                                                            val intValue = it.toIntOrNull() ?: return@MyTextField
                                                            o.put("type", listOf(type.getInt(0),type.getInt(1),intValue))
                                                            filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                                        },
                                                        modifier = Modifier
                                                            .width(40.dp),
                                                        keyboardOptions = KeyboardOptions(
                                                            keyboardType = KeyboardType.Number
                                                        ),


                                                    )
                                            }
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Show Songs with")
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
                                                    filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                                },
                                                Modifier.weight(1f)
                                            )
                                        }
                                    }
                                    Tag.Type.FLOAT, Tag.Type.DATETIME, Tag.Type.DATE, Tag.Type.TIME -> {
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
                                                filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                                            },
                                            Modifier.weight(1f)
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
                Button(onClick = {
                    arr.put(JSONObject().apply {
                        put("filter", false)
                        put("tag", Song._TPM)
                        put("text", "Duration")
                    })
                    filterOptions.value = JSONArray(filterOptions.value.toString()) // trigger re-render
                }) {
                    Text("Add Item")
                }
            }
        }

    }

}

@Composable
fun HeaderCell(o:JSONObject, tag: Tag, value: Any?, value2: Any?, onValueChange: (Any?) -> Unit = {}, onValue2Change: (Any?) -> Unit = {}) {
    val text = o.optString("text", "")
    val textSize = o.optInt("textSize", 16)
    Row(verticalAlignment = Alignment.CenterVertically) {
        if(o.getBoolean("filter")) {
            val typeArr = o.getJSONArray("type")
            val types = List(typeArr.length()) { typeArr.getInt(it) }
            when(tag.type) {
                Tag.Type.INT -> {
                    val intVal = value as? Int ?: -1
                    if(types[0] <= 2) {
                        if(types.size != 3) {
                            Text("INVALID")
                            return
                        }
                        Text("$text: ", )
                        for(k in 0 until types[2]) {
                            var filled = k <= intVal
                            if(types[0] == 2 && types[1] == 1) filled = k >= intVal
                            
                            if(types[0] == 0) Text(if(filled) "★" else "☆", fontSize = textSize.sp)
                            else if(types[0] == 1) Text("♫", fontSize = textSize.sp, color = if(filled) MaterialTheme.colorScheme.onBackground else Color.Gray)
                            else if(types[0] == 2) Text("$k", fontSize = textSize.sp, color = if(filled) MaterialTheme.colorScheme.onBackground else Color.Gray)
                        }
                    } else if(types[0] == 3) {
                        if(types.size != 3) {
                            Text("INVALID")
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
                            Text(if(types[1] == 4 || types[1] == 7) " < " else " ≤ ", fontSize = textSize.sp)
                        }
                        Text("$text", fontSize = textSize.sp)
                        Text(
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
                        Text("INVALID")
                    }
                }
                Tag.Type.FLOAT -> {
                    if(types.size != 2) {
                        Text("INVALID")
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
                        Text(if(types[1] == 4 || types[1] == 7) " < " else " ≤ ", fontSize = textSize.sp)
                    }
                    Text("$text", fontSize = textSize.sp)
                    Text(
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
                }
                Tag.Type.BOOL -> {
                    Text("$text: ", fontSize = textSize.sp)
                    Text(listOf("All", "Yes", "No")[value as? Int ?: 0], 
                    fontSize = textSize.sp,
                    modifier = Modifier.clickable {
                        val newValue = ((value as? Int ?: 0) + 1) % 3 as Int
                        onValueChange(newValue)
                    })
                } 
                Tag.Type.DATETIME, Tag.Type.DATE, Tag.Type.TIME -> {
                    if(types.size != 2) {
                        Text("INVALID")
                        return
                    }
                    if(types[0] > 3) {
                        // input
                        TextField(
                            value = "$value",
                            placeholder = { Text(DateTimeUtil.getPattern(tag.type), fontSize = textSize.sp) },
                            onValueChange = { onValueChange(DateTimeUtil.parse(it, tag.type)) },
                            modifier = Modifier.width(40.dp),
                            textStyle = TextStyle(fontSize = textSize.sp),
                        )
                        Text(if(types[1] == 4 || types[1] == 7) " < " else " ≤ ", fontSize = textSize.sp)
                    }
                    Text("$text", fontSize = textSize.sp)
                    Text(
                            if(types[0] == 3) " > "
                            else if(types[0] == 1) " ≥ "
                            else if(types[0] == 2 || types[0] == 4 ||types[0] == 6) " < " 
                            else " ≤ ",
                            fontSize = textSize.sp)
                    TextField(
                        value = "$value2",
                        placeholder = { Text(DateTimeUtil.getPattern(tag.type), fontSize = textSize.sp) },
                        onValueChange = { onValue2Change(DateTimeUtil.parse(it, tag.type)) },
                        modifier = Modifier.width(40.dp),
                        textStyle = TextStyle(fontSize = textSize.sp),
                    )
                } 
                Tag.Type.STRING -> {
                    Text("$text: ", fontSize = textSize.sp)
                    TextField(
                        value = "$value",
                        onValueChange = { onValueChange(it) },
                        textStyle = TextStyle(fontSize = textSize.sp),
                        modifier = Modifier.width(100.dp),
                    )
                } 
                else -> {
                    Text("Unknown Type", fontSize = textSize.sp)
                }
            }
            
        } else { // sorter
            val suffix = when(value) {
                1 -> " ↑"
                2 -> " ↓"
                else -> ""
            }
            Text(o.optString("text", "") + suffix, 
                fontSize = textSize.sp,
                modifier = Modifier.clickable {
                    val newValue = when(value) {
                        1 -> 2
                        else -> 1
                    }
                    onValueChange(newValue)
                }
            )
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
                put("type", JSONArray(listOf(0,0,5)))
                put("text", "Rating")
            })
        })
        put(JSONArray().apply {
            put(JSONObject().apply {
                put("filter", false)
                put("tag", Song._TPM)
                put("text", "Duration")
            })
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