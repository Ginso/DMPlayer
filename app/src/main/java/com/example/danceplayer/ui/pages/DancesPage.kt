package com.example.danceplayer.ui.pages

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.danceplayer.MainActivity
import com.example.danceplayer.ui.subpages.dances.DanceSongsPage
import com.example.danceplayer.util.ClickBox
import com.example.danceplayer.util.MusicLibrary

@Composable
fun DancesPage() {
    val selectedDance = remember { mutableStateOf<String?>(null) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp, 4.dp)
    ) {
        val dances = MusicLibrary.songs.value.map { it.getDance() }

        val countPerDance = dances.groupingBy { it }.eachCount()
        val danceList = dances.distinct().sorted()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (dance in danceList) {
                val count = countPerDance[dance] ?: 0
                
                ClickBox(
                    onClick= {
                            MainActivity.addPage(DanceSongsPage(dance))
                        },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    ) {
                        Text(
                            text = dance,
                            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$count Songs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF9500) // #FF9500
                        )
                    }
                }
            }
        }
    }
}
