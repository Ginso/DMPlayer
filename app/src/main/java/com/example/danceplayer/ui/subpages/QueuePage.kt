package com.example.danceplayer.ui.subpages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.danceplayer.util.Player
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.danceplayer.R
import com.example.danceplayer.ui.Fragment
import kotlin.math.roundToInt

class QueuePage : Fragment() {
    override fun getTitle(): String {
        return "Player Queue"
    }

    @Composable
    override fun Content() {
        val queue = Player.getPlayList()
        val profile = PreferenceUtil.getCurrentProfile()
        val itemLayout = profile.itemLayoutQueue
        val contextEntries = listOf(
            ContextItem.EDIT
        )

        Main {
            if(queue.isEmpty()) {
                Text("The queue is currently empty")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for((index, song) in queue.withIndex()) {
                        
                        SongItem(
                            song, 
                            itemLayout,
                            Modifier.fillMaxWidth(),
                            contextEntries
                        ) {
                            Player.goTo(index)
                        }
                    }
                }
            }
        }
    }
}