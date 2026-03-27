package com.example.danceplayer.ui.subpages

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.danceplayer.R
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.util.Player
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
        val context = LocalContext.current

        val iconSize = 46.dp
        Main(center = true) {

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
                    style = TextStyle(fontSize = 16.sp, fontStyle = FontStyle.Companion.Italic),
                    modifier = Modifier.Companion
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    color = Color.Companion.LightGray
                )

                //speed
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Text(
                        "-10%",
                        modifier = Modifier.Companion
                            .clickable {
                                Player.changeSpeed(-0.1f)
                            }
                    )
                    Text(
                        "-1%",
                        modifier = Modifier.Companion
                            .clickable {
                                Player.changeSpeed(-0.01f)
                            }
                    )
                    Text(text = "${(speed * 100).toInt()}%", style = TextStyle(fontSize = 24.sp))
                    Text(
                        "+1%",
                        modifier = Modifier.Companion
                            .clickable {
                                Player.changeSpeed(0.01f)
                            }
                    )
                    Text(
                        "+10%",
                        modifier = Modifier.Companion
                            .clickable {
                                Player.changeSpeed(0.1f)
                            }
                    )
                }
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Text("25%")
                    Slider(
                        value = speed.toFloat(),
                        valueRange = 0.25f..1.5f,
                        onValueChange = { Player.setSpeed((it * 100).roundToInt() / 100f) },
                        modifier = Modifier.Companion.weight(1f).padding(horizontal = 8.dp)
                    )
                    Text("150%")
                }
                Spacer(modifier = Modifier.Companion.height(16.dp))

                Box(
                    modifier = Modifier.Companion
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .border(1.dp, Color.Companion.Gray, shape = RoundedCornerShape(10.dp))
                ) {
                    Text(
                        text = "${DateTimeUtil.formatDuration(position)} | ${
                            DateTimeUtil.formatDuration(
                                currentSong!!.getDuration()
                            )
                        }",
                        modifier = Modifier.Companion
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Slider(
                    value = position.toFloat(),
                    valueRange = 0f..currentSong!!.getDuration().toFloat(),
                    onValueChange = { Player.seekTo(it.toLong()) },
                    modifier = Modifier.Companion.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    IconButton(
                        onClick = { Player.previous() },
                        modifier = Modifier.Companion.size(iconSize),
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_previous),
                            contentDescription = "Previous",
                            modifier = Modifier.Companion.size(iconSize)
                        )
                    }
                    IconButton(
                        onClick = {
                            if (isPlaying) Player.pause() else Player.play(context)
                        },
                        modifier = Modifier.Companion.size(iconSize),
                    ) {
                        Icon(
                            painter = painterResource(id = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                            contentDescription = "Play/Pause",
                            modifier = Modifier.Companion.size(iconSize)
                        )
                    }
                    IconButton(
                        onClick = { Player.next() },
                        modifier = Modifier.Companion.size(iconSize),
                        enabled = Player.currentIndex < Player.getPlayList().size - 1
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_next),
                            contentDescription = "Next",
                            modifier = Modifier.Companion.size(iconSize)
                        )
                    }
                }

                val playerMode by Player.playerModeState
                // material-icons-extended
                val modes = listOf(
                    Triple(ExoPlayer.REPEAT_MODE_OFF, Icons.Default.Repeat, Color.Gray),
                    Triple(ExoPlayer.REPEAT_MODE_ALL, Icons.Default.Repeat, MaterialTheme.colorScheme.onBackground),
                    Triple(ExoPlayer.REPEAT_MODE_ONE, Icons.Default.RepeatOne, MaterialTheme.colorScheme.onBackground)
                )
                val idx = modes.indexOfFirst { it.first == playerMode }
                IconButton(
                    onClick = {
                        val nextMode = modes[(idx + 1) % modes.size].first
                        Player.setPlayerMode(nextMode)
                    },
                    modifier = Modifier.Companion.size(36.dp)
                ) {
                    Icon(
                        imageVector = modes[idx].second,
                        contentDescription = "Player Mode",
                        tint = modes[idx].third,
                        modifier = Modifier.Companion.size(24.dp)
                    )
                }
            }

        }
    }
}