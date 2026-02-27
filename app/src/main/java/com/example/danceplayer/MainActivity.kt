package com.example.danceplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding

// additional layout/imports used in BottomBar
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import com.example.danceplayer.ui.theme.DancePlayerTheme
import com.example.danceplayer.ui.pages.DancesPage
import com.example.danceplayer.ui.pages.PlaylistsPage
import com.example.danceplayer.ui.pages.SettingsPage
import com.example.danceplayer.util.PreferenceUtil
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.Player
import com.example.danceplayer.util.DateTimeUtil
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // suspend initialization: runs asynchronously
        lifecycleScope.launch {
            PreferenceUtil.initialize(this)
            Player.initialize(this)
            MusicLibrary.initialize(this@MainActivity)
        }

        enableEdgeToEdge()
        setContent {
            DancePlayerTheme {
                MainScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Player.release()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val selectedPage = remember { mutableStateOf(2) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                onPageSelected = { page ->
                    selectedPage.value = page
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Dance Player") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            bottomBar = {
                BottomBar()
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedPage.value) {
                    0 -> DancesPage()
                    1 -> PlaylistsPage()
                    2 -> SettingsPage()
                }
            }
        }
    }
}

@Composable
fun NavigationDrawerContent(onPageSelected: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            "Dances",
            modifier = Modifier
                .padding(16.dp)
                .clickable { onPageSelected(0) }
        )
        Text(
            "Playlists",
            modifier = Modifier
                .padding(16.dp)
                .clickable { onPageSelected(1) }
        )
        Text(
            "Settings",
            modifier = Modifier
                .padding(16.dp)
                .clickable { onPageSelected(2) }
        )
    }
}

@Composable
fun BottomBar() {
    val currentSong by Player.currentSongState
    val isPlaying by Player.isPlayingState
    val speed by Player.speedState
    val position by Player.positionState

    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { Player.previous() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_previous),
                    contentDescription = "Previous"
                )
            }
            Text("-", modifier = Modifier.clickable {
                if (Player.speed >= 0.05f)
                    Player.setSpeed(Player.speed - 0.05f)

            })
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentSong?.let { "${it.getTitle()} - ${it.getArtist()}" } ?: "No song",
                    style = TextStyle(fontSize = 12.sp)
                )
                currentSong?.let { song ->
                    Box(modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .background(
                            color = Color.DarkGray,
                            shape = RoundedCornerShape(10.dp)
                        )
                    ) {
                        Text(
                            text = song.getDance(),
                            style = TextStyle(fontSize = 12.sp),
                            background = Color.LightGray,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            color = Color.LightGray

                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier
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
                            text = "Speed: ${"%d".format((speed * 100).toInt())}%",
                            style = TextStyle(fontSize = 12.sp),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            Text("+", modifier = Modifier.clickable {
                if (Player.speed <= 3.95f)
                    Player.setSpeed(Player.speed + 0.05f)

            })
            Button(
                onClick = {
                    if (speed > 4.0f) return@Button
                    Player.setSpeed(speed + 0.05f)
                }
            ) {
                Text("+")
            }
            IconButton(onClick = { Player.next() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_next),
                    contentDescription = "Next"
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DancePlayerTheme {
        MainScreen()
    }
}