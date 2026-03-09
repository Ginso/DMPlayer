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