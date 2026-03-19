package com.example.danceplayer.ui.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.danceplayer.model.Song
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.Player
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource

@Composable
fun PlayerPage(onClose: () -> Unit) {
    val currentSong by Player.currentSongState
    val isPlaying by Player.isPlayingState
    val speed by Player.speedState
    val position by Player.positionState

    val iconSize = 46.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(

                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(currentSong == null) {
                    Text("No song is currently playing", style = MaterialTheme.typography.titleLarge)
                } else {

                    Text(
                        text = currentSong.getTitle(),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = currentSong.getArtist(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = currentSong.getDance(),
                        style = TextStyle(fontSize = 12.sp, fontStyle = FontStyle.Italic),
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        color = Color.LightGray
                    )

                    //speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("-10%",
                            modifier = Modifier
                                .clickable {
                                    Player.changeSpeed(-0.1f)
                                }
                        )
                        Text("-1%",
                            modifier = Modifier
                                .clickable {
                                    Player.changeSpeed(-0.01f)
                                }
                        )
                        Text(text = "${(speed * 100).toInt()}%")
                        Text("+1%",
                            modifier = Modifier
                                .clickable {
                                    Player.changeSpeed(0.01f)
                                }
                        )
                        Text("+10%",
                            modifier = Modifier
                                .clickable {
                                    Player.changeSpeed(0.1f)
                                }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("25%")
                        Slider(
                            value = speed.toFloat(),
                            valueRange = 0.25f..8.0f,
                            onValueChange = { Player.setSpeed((it * 100).roundToInt() / 100f) },
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        )
                        Text("800%")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for(i in 0..4) {
                            Button(
                                onClick = {
                                    Player.setSpeed(0.8f + i * 0.1f)
                                }
                            ) {
                                Text("${80 + i * 10}%")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
                    ) {
                        Text(
                            text = "${DateTimeUtil.formatDuration(position)} | ${DateTimeUtil.formatDuration(song.getDuration())}",
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    Slider(
                        value = position.toFloat(),
                        valueRange = 0f..song.getDuration().toFloat(),
                        onValueChange = { Player.seekTo(it.toLong()) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { Player.previous() },
                            modifier = Modifier.size(iconSize),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_previous),
                                contentDescription = "Previous",
                                modifier = Modifier.size(iconSize)
                            )
                        }
                        IconButton(
                            onClick = {
                                if (isPlaying) Player.pause() else Player.play()
                            },
                            modifier = Modifier.size(iconSize),
                        ) {
                            Icon(
                                painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                contentDescription = "Play/Pause",
                                modifier = Modifier.size(iconSize)
                            )
                        }
                        IconButton(
                            onClick = { Player.next() },
                            modifier = Modifier.size(iconSize),
                            enabled = Player.currentIndex < Player.getPlayList().size - 1
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_next),
                                contentDescription = "Next",
                                modifier = Modifier.size(iconSize)
                            )
                        }
                    }
                }
            }
        }
    }
}
