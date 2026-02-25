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
import com.example.danceplayer.ui.theme.DancePlayerTheme
import com.example.danceplayer.ui.pages.DancesPage
import com.example.danceplayer.ui.pages.PlaylistsPage
import com.example.danceplayer.ui.pages.SettingsPage
import com.example.danceplayer.util.PreferenceUtil
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceUtil.initialize(this)
        enableEdgeToEdge()
        setContent {
            DancePlayerTheme {
                MainScreen()
            }
        }
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
    BottomAppBar(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Zeile 1", style = TextStyle(fontSize = 12.sp))
            Text("Zeile 2", style = TextStyle(fontSize = 12.sp))
            Text("Zeile 3", style = TextStyle(fontSize = 12.sp))
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