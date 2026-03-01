package com.example.danceplayer

// additional layout/imports used in BottomBar
import DateTimeUtil
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.danceplayer.ui.pages.DancesPage
import com.example.danceplayer.ui.pages.PlaylistsPage
import com.example.danceplayer.ui.pages.SettingsPage
import com.example.danceplayer.ui.theme.DancePlayerTheme
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.Player
import com.example.danceplayer.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // suspend initialization: runs asynchronously
        lifecycleScope.launch {
            PreferenceUtil.initialize(this@MainActivity)
            Player.initialize(this@MainActivity)
            withContext(Dispatchers.IO) {
                MusicLibrary.initialize(this@MainActivity)
            }
        }

        val profile = PreferenceUtil.getCurrentProfile()
        if (profile.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        if (profile.showOnLock) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
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
    val controlWidth = 100.dp
    val iconSize = 38.dp

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
                    onClick = { Player.previous() },
                    modifier = Modifier.size(iconSize),
                    enabled = currentSong != null
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_previous),
                        contentDescription = "Previous",
                        modifier = Modifier.size(iconSize)
                    )
                }
            }

            // center song info with weight to take remaining space
            Column(
                modifier = Modifier.weight(1f),
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
                    onClick = {
                        if (isPlaying) Player.pause() else Player.play()
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
                IconButton(
                    onClick = { Player.next() },
                    modifier = Modifier.size(iconSize),
                    enabled = currentSong != null && Player.currentIndex < Player.playlist.size - 1
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



@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    DancePlayerTheme {
        MainScreen()
    }
}