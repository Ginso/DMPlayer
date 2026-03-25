package com.example.danceplayer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.danceplayer.MainActivity
import com.example.danceplayer.R
import com.example.danceplayer.ui.subpages.PlayerPage
import com.example.danceplayer.ui.subpages.QueuePage
import com.example.danceplayer.util.Player

@Composable
fun BottomBar() {
    val currentSong by Player.currentSongState
    val isPlaying by Player.isPlayingState
    val speed by Player.speedState
    val position by Player.positionState
    val controlWidth = 100.dp
    val iconSize = 38.dp
    val iconSize2 = 30.dp
    val context = LocalContext.current

    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // left controls (fixed width)
            Row(
                modifier = Modifier.width(controlWidth),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { Main.addPage(QueuePage()) },
                    modifier = Modifier.size(iconSize),
                    enabled = currentSong != null
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_queue_music),
                        contentDescription = "Player Queue",
                        modifier = Modifier.size(iconSize)
                    )
                }
                IconButton(
                    onClick = { Player.previous() },
                    modifier = Modifier.size(200.dp),
                    enabled = currentSong != null
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_previous),
                        contentDescription = "Previous",
                        modifier = Modifier.size(iconSize2)
                    )
                }
            }

            // center song info with weight to take remaining space
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable() {
                        Main.addPage(PlayerPage())
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentSong?.let { "${it.getTitle()} - ${it.getArtist()}" } ?: "No song",
                    style = TextStyle(fontSize = 12.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                currentSong?.let { song ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .background(
                                color = Color.DarkGray,
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Text(
                            text = song.getDance(),
                            style = TextStyle(fontSize = 12.sp, fontStyle = FontStyle.Italic),
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Color.LightGray

                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = "${DateTimeUtil.formatDuration(position)} | ${DateTimeUtil.formatDuration(song.getDuration())}",
                                style = TextStyle(fontSize = 12.sp),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${"%d".format((speed * 100).toInt())}%",
                            style = TextStyle(fontSize = 12.sp),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // right controls (fixed width)
            Row(
                modifier = Modifier.width(controlWidth),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,

                ) {
                IconButton(
                    onClick = { Player.next() },
                    modifier = Modifier.size(iconSize2),
                    enabled = currentSong != null && Player.currentIndex < Player.getPlayList().size - 1
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_next),
                        contentDescription = "Next",
                        modifier = Modifier.size(iconSize2)
                    )
                }
                IconButton(
                    onClick = {
                        if (isPlaying) Player.pause() else Player.play(context)
                    },
                    modifier = Modifier.size(iconSize),
                    enabled = currentSong != null
                ) {
                    Icon(
                        painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                        contentDescription = "Play/Pause",
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}
