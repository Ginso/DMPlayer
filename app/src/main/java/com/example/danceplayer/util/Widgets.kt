package com.example.danceplayer.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun SimpleDropDown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SimpleDropDownImpl(options, selectedOption, { idx:Int -> onOptionSelected(options[idx]) }, modifier)
}

@Composable
fun SimpleDropDown(
    options: List<String>,
    selectedOption: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    SimpleDropDownImpl(options, options[selectedOption], onOptionSelected, modifier)
}

@Composable
private fun SimpleDropDownImpl(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var buttonWidth by remember { mutableIntStateOf(0) }

    Box(modifier) {
        OutlinedButton(
            onClick = { expanded = true },

            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    buttonWidth = coordinates.size.width
                },
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Text(selectedOption)
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .border(1.dp, color = MaterialTheme.colorScheme.onBackground)
                .width(with(LocalDensity.current) { buttonWidth.toDp() })
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(index)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun MyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions:KeyboardOptions = KeyboardOptions.Default
) {
    val color = MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = color,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            }
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurfaceVariant),
            keyboardOptions = keyboardOptions,

            modifier = Modifier.padding(all=4.dp)
        )
    }
}

@Composable
fun SongItem(song: Song, layout:JSONObject, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(all=4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongItemInner(song, layout, Modifier.weight(1f))
            
            Text("•••")
        }
    }
}

@Composable
private fun SongItemInner(song: Song, layout:JSONObject, modifier: Modifier = Modifier) {
    var type = layout.getInt("type")
    var items = layout.getJSONArray("items")
    Container(type,
        layout.optInt("arrangement", 0),
        layout.optInt("alignment", 1-type),
        modifier) {
        for(i in 0..items.length()-1) {
            val item = items.getJSONObject(i)
            when(item.getInt("type")) {
                ElementType.TAG -> Text(song.getTitle(), modifier = Modifier.weight(1f))
                ElementType.SPACE -> {
                    val size = item.getInt("size")
                    val modifier = if(size == 0) Modifier.weight(1f) else if(type == ElementType.ROW) Modifier.width(size.dp) else Modifier.height(size.dp)
                    Spacer(modifier = modifier)
                }
                else -> {
                    var modifier = Modifier
                    if(item.optInt("width", 0) == 1) modifier = if(type == ElementType.ROW) modifier.weight(1f) else modifier.fillMaxWidth()
                    if(item.optInt("height", 0) == 1) modifier = if(type == ElementType.ROW) modifier.fillMaxHeight() else modifier.weight(1f)

                    SongItemInner(song, item, modifier)
                }
            }
        }
    }
}

@Composable
private fun TagWidget(song: Song, layout:JSONObject) {
    Row {
        val tagName = layout.getString("tag")
        val tag = MusicLibrary.getAllTagsMap()[tagName]
        if(tag == null) {
            Text("Invalid tag: $tagName", color = Color.Red)
            return@Row
        }
        val value = song.getTagValue(tag)

        val prefix = layout.optString("prefix", "")
        val suffix = layout.optString("suffix", "")
        val textSize = layout.optInt("textSize", 16)
        val textStyle = TextStyle(
            color = if(layout.optBoolean("gray", false)) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
            weight = if(layout.optBoolean("bold", false)) FontWeight.Bold else FontWeight.Normal,
            italic = layout.optBoolean("italic", false),
            underline = layout.optBoolean("underline", false),
            fontSize = textSize.sp
        )
        if(tag.type == Tag.Type.BOOL) {
            Text(layout.optString(if(value as Boolean) "trueText" else "falseText", ""), style = textStyle)
            return@Row
        }
        if(prefix.isNotEmpty()) Text(prefix, style = textStyle)
        if(tag.type == Tag.Type.INT) {
            val maxValue = layout.optInt("maxValue", 5)
            val intVal = value as Int
            when(layout.optInt("display", 2)) {
                0 -> {
                    var text = padEnd(text, intVal, '★')
                    text = padEnd(text, maxValue, '☆')
                    //for(i in 1..intVal) text += "★"
                    //for(i in intVal+1..maxValue) text += "☆"
                    Text(text, style = textStyle)
                }
                1 -> {
                    var text = padEnd(text, intVal, '♫')
                    Text(text, style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    text = padEnd(text, maxValue - intVal, '♫')
                    Text(text, style = textStyle.copy(color = Color.Gray))
                }
                else -> Text(intVal.toString(), style = textStyle)
            }
        } else {
            Text("$value", style = textStyle)
        }
        if(suffix.isNotEmpty()) Text(suffix, style = textStyle)
    }
}

@Composable
private fun Container(type: Int, alignment: Int): @Composable (Modifier, @Composable () -> Unit) -> Unit {
    return when(type) {
        ElementType.ROW -> { m, c -> Row(
            modifier = Modifier,
            verticalAlignment = when(alignment) {
                0 -> Alignment.Start
                1 -> Alignment.Center
                2 -> Alignment.End
                else -> null
            }, 
            content = c
        )}
        ElementType.COLUMN -> { m, c -> Column(
            modifier = Modifier,
            horizontalAlignment = when(alignment) {
                0 -> Alignment.Start
                1 -> Alignment.Center
                2 -> Alignment.End
                else -> null
            }, 
            content = c
        )}
        else -> { m, c -> Box(modifier = m, content = c) }
    }
}