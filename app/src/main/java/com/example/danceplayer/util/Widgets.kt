package com.example.danceplayer.util

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.danceplayer.MainActivity
import com.example.danceplayer.model.ContextItem
import com.example.danceplayer.model.Song
import com.example.danceplayer.model.Tag
import com.example.danceplayer.ui.Main
import com.example.danceplayer.ui.subpages.settings.ElementType
import com.google.common.base.Strings.padEnd
import kotlinx.coroutines.delay
import org.json.JSONObject

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
fun SongItem(song: Song, layout: JSONObject,
             modifier: Modifier = Modifier,
             contextEntries: List<ContextItem> = emptyList(),
             index: Int = 0,
             playingAfter: Long = 0L,
             onClick: () -> Unit = {}
) {
    val showContext = remember { mutableStateOf(false) }
    var textPosition by remember { mutableStateOf(Offset.Zero) }
    var textSize by remember { mutableStateOf(IntSize.Zero) }
    var overlaySize by remember { mutableStateOf(IntSize.Zero) }
    var currentSong by Player.currentSongState
    val isCurrent = currentSong == song
     
    ClickBox(
        onClick = onClick,
        backgroundColor = if(isCurrent) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(all=8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongItemInner(song, layout, Modifier.weight(1f), index, playingAfter)
            
            Text("•••", modifier = Modifier
                .clickable() {
                    showContext.value = true
                    Main.popupOverlay.value = true
                    Main.onDismissPopup = {
                        showContext.value = false
                    }
                }
                .onGloballyPositioned {
                    textPosition = it.positionInParent()
                    textSize = it.size
                }
            )
        }

        if(showContext.value) {

            Popup(
                offset = IntOffset(
                    (textPosition.x + textSize.width - overlaySize.width).toInt(),
                    (textPosition.y + textSize.height).toInt()
                ),
                onDismissRequest = {
                    showContext.value = false
                    Main.popupOverlay.value = false
                    Main.onDismissPopup = {}
                }
            ) {
                Box(
                    modifier = Modifier.onGloballyPositioned {
                        overlaySize = it.size
                    }
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(4.dp))
                ) {
                    Column {
                        contextEntries.forEach { entry ->
                            Text(
                                text = entry.text,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        entry.onClick(song)
                                        showContext.value = false
                                        Main.popupOverlay.value = false
                                        Main.onDismissPopup = {}
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SongItemInner(song: Song,
                          layout:JSONObject,
                          modifier: Modifier = Modifier,
                          index: Int = 0,
                          playingAfter: Long = 0L
) {
    val type = layout.getInt("type")
    val items = layout.getJSONArray("items")
    Container(type,
        layout.optInt("alignment", 1-type),
        layout.optInt("space", 8),
        modifier) { weight ->
        for(i in 0..items.length()-1) {
            val item = items.getJSONObject(i)
            when(item.getInt("type")) {
                ElementType.TAG -> TagWidget(song, item, index, playingAfter)
                ElementType.SPACE -> {
                    val size = item.getInt("size")
                    val modifier = if(size == 0) weight(Modifier) else if(type == ElementType.ROW) Modifier.width(size.dp) else Modifier.height(size.dp)
                    Spacer(modifier = modifier)
                }
                else -> {
                    var modifier: Modifier = Modifier
                    if(item.optInt("width", 0) == 1) modifier = if(type == ElementType.ROW) weight(modifier) else modifier.fillMaxWidth()
                    if(item.optInt("height", 0) == 1) modifier = if(type == ElementType.ROW) modifier.fillMaxHeight() else weight(modifier)

                    SongItemInner(song, item, modifier, index, playingAfter)
                }
            }
        }
    }
}

@Composable
private fun TagWidget(song: Song, layout:JSONObject,
                      index: Int = 0,
                      playingAfter: Long = 0L) {
    Row {
        val tagName = layout.getString("tag")
        val tag = MusicLibrary.tagMap.value[tagName]
        if(tag == null) {
            Text("Invalid tag: $tagName", color = Color.Red)
            return@Row
        }
        val value = when(tag.name) {
            Song._POSITION -> index+1
            Song._PLAYING_AFTER -> DateTimeUtil.formatDuration(playingAfter)
            else -> song.getTagValue(tag)
        }

        val prefix = layout.optString("prefix", "")
        val suffix = layout.optString("suffix", "")
        val textSize = layout.optInt("textSize", 16)
        val textStyle = TextStyle(
            color = if(layout.optBoolean("gray", false)) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,

            fontWeight = if(layout.optBoolean("bold", false)) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if(layout.optBoolean("italic", false)) FontStyle.Italic else null,
            textDecoration = if(layout.optBoolean("underline", false)) TextDecoration.Underline else null,
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
                    var text = padEnd("", intVal, '★')
                    text = padEnd(text, maxValue, '☆')
                    //for(i in 1..intVal) text += "★"
                    //for(i in intVal+1..maxValue) text += "☆"
                    Text(text, style = textStyle)
                }
                1 -> {
                    var text = padEnd("", intVal, '♫')
                    Text(text, style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    text = padEnd("", maxValue - intVal, '♫')
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
private fun Container(
    type: Int,
    alignment: Int,
    space: Int,
    modifier: Modifier = Modifier,
    content: @Composable ((Modifier) -> Modifier) -> Unit
) {
    when (type) {
        ElementType.COLUMN -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(space.dp),
            horizontalAlignment = when (alignment) {
                0 -> Alignment.Start
                1 -> Alignment.CenterHorizontally
                2 -> Alignment.End
                else -> Alignment.Start
            }
        ) {
            content { it.weight(1f) }
        }

        else -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(space.dp),
            verticalAlignment = when (alignment) {
                0 -> Alignment.Top
                1 -> Alignment.CenterVertically
                2 -> Alignment.Bottom
                else -> Alignment.CenterVertically
            }
        ) {
            content { it.weight(1f) }
        }
    }
}

@Composable
fun ClickBox(onClick: () -> Unit, 
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    backgroundColorClicked: Color = backgroundColor.copy(alpha = 0.85f),
    shape: Shape = RoundedCornerShape(16.dp),
    tapFeedbackDurationMs: Long = 120,
    content: @Composable BoxScope.() -> Unit) {

        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        var showTapFeedback by remember { mutableStateOf(false) }
        LaunchedEffect(showTapFeedback) {
            if (showTapFeedback) {
                delay(tapFeedbackDurationMs)
                showTapFeedback = false
                onClick()
            }
        }
        val itemBackgroundColor by animateColorAsState(
            targetValue = if (isPressed || showTapFeedback) {
                backgroundColorClicked
            } else {
                backgroundColor
            },
            label = "danceItemBackground"
        )
        Box(
            modifier = modifier
                .background(itemBackgroundColor, shape)
                .clickable(interactionSource = interactionSource, indication = null) {
                    showTapFeedback = true
                },
            content = content
        )
}