package com.example.danceplayer.ui.pages

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

class PlayerPage : Fragment() {
    override fun getTitle(): String {
        return "Current Song"
    }

    @Composable
    override fun Content() {
        val currentSong by Player.currentSongState
        val isPlaying by Player.isPlayingState
        val speed by Player.speedState
        val position by Player.positionState

        val iconSize = 46.dp
        Main {

            if(currentSong == null) {
                Text("No song is currently playing", style = MaterialTheme.typography.titleLarge)
            } else {

                Text(
                    text = currentSong!!.getTitle(),
                    style = TextStyle(fontSize = 32.sp)
                )
                Text(
                    text = currentSong!!.getArtist(),
                    style = TextStyle(fontSize = 24.sp)
                )
                Text(
                    text = currentSong!!.getDance(),
                    style = TextStyle(fontSize = 16.sp, fontStyle = FontStyle.Italic),
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color.LightGray
                )

                //speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
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
                    Text(text = "${(speed * 100).toInt()}%", style = TextStyle(fontSize = 24.sp))
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
                        valueRange = 0.25f..1.5f,
                        onValueChange = { Player.setSpeed((it * 100).roundToInt() / 100f) },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Text("150%")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = "${DateTimeUtil.formatDuration(position)} | ${DateTimeUtil.formatDuration(currentSong!!.getDuration())}",
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Slider(
                    value = position.toFloat(),
                    valueRange = 0f..currentSong!!.getDuration().toFloat(),
                    onValueChange = { Player.seekTo(it.toLong()) },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
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
