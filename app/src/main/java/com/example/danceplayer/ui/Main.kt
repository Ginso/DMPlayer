package com.example.danceplayer.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.danceplayer.ui.pages.DancesPage
import com.example.danceplayer.ui.pages.PlaylistsPage
import com.example.danceplayer.ui.pages.SettingsPage
import com.example.danceplayer.ui.theme.DancePlayerTheme
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.Player

object Main {
    var isInitialized = false

    val pageTitles = listOf(
        "Dances",
        "Playlists",
        "Settings"
    )

    val selectedPage = mutableIntStateOf(0)

    val pageStack = mutableStateOf<List<Fragment>>(emptyList())
    val popupOverlay = mutableStateOf(false)
    var onDismissPopup: () -> Unit = {}

    fun addPage(page: Fragment) {
        for (existingPage in pageStack.value) {
            if (existingPage.sameType(page)) {
                pageStack.value = pageStack.value - existingPage + existingPage
                return
            }
        }
        pageStack.value = pageStack.value + page
    }

    fun popLastPage() {
        if (pageStack.value.isNotEmpty()) {
            pageStack.value = pageStack.value.dropLast(1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val selectedPage = Main.selectedPage
    val isInitializing by MusicLibrary.isInitializing
    val pageStack by Main.pageStack
    val isPlaying by Player.isPlayingState
    val context = LocalContext.current
    val showCloseConfirm = remember { mutableStateOf(false) }

    if (isInitializing) {
        LoadingScreen()
        return
    }

    BackHandler {
        when {
            showCloseConfirm.value -> showCloseConfirm.value = false
            pageStack.isNotEmpty() -> Main.popLastPage()
            isPlaying -> showCloseConfirm.value = true
            else -> (context as? ComponentActivity)?.finish()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            ) {
                PageSelectionBar(
                    selectedPage = selectedPage.intValue,
                    onPageSelected = { page ->
                        selectedPage.value = page
                        Main.pageStack.value = emptyList()
                    }
                )
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(pageStack.lastOrNull()?.getTitle() ?: Main.pageTitles[selectedPage.intValue])
                    },
                    navigationIcon = {
                        if (pageStack.isNotEmpty()) {
                            IconButton(onClick = Main::popLastPage) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    }
                )
            }
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
            when (selectedPage.intValue) {
                0 -> DancesPage()
                1 -> PlaylistsPage()
                2 -> SettingsPage()
            }
            for (page in pageStack) {
                key(page.getStackEntryId()) {
                    page.Content()
                }
            }
        }
    }

    if (showCloseConfirm.value) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm.value = false },
            title = { Text("Close App?") },
            text = { Text("Music is currently playing. Do you really want to close the app?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCloseConfirm.value = false
                        (context as? ComponentActivity)?.finish()
                    }
                ) {
                    Text("Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirm.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if(Main.popupOverlay.value) {
        Box (
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    Main.popupOverlay.value = false
                    Main.onDismissPopup()
                    Main.onDismissPopup = {}
                }
        )
    }


}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Musikbibliothek wird geladen...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun PageSelectionBar(selectedPage: Int, onPageSelected: (Int) -> Unit) {
    Surface(shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Main.pageTitles.forEachIndexed { idx, page ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (idx == selectedPage) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                        .clickable { onPageSelected(idx) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = page,
                        color = if (idx == selectedPage) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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