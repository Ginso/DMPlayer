package com.example.danceplayer

// additional layout/imports used in BottomBar
import DateTimeUtil
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.lifecycleScope
import com.example.danceplayer.ui.BottomBar
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.ui.pages.DancesPage
import com.example.danceplayer.ui.subpages.PlayerPage
import com.example.danceplayer.ui.pages.PlaylistsPage
import com.example.danceplayer.ui.pages.SettingsPage
import com.example.danceplayer.ui.subpages.QueuePage
import com.example.danceplayer.ui.theme.DancePlayerTheme
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.Player
import com.example.danceplayer.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

class MainActivity : ComponentActivity() {
    companion object {

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
    private var initMusicJob: kotlinx.coroutines.Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, com.example.danceplayer.service.PlaybackService::class.java))

        if(!isInitialized) {
            // suspend initialization: runs asynchronously
            initMusicJob = lifecycleScope.launch {
                PreferenceUtil.initialize(this@MainActivity)
                Player.initialize(this@MainActivity)
                withContext(Dispatchers.IO) {
                    MusicLibrary.initialize(this@MainActivity)
                }
            }
        }

        val profile = PreferenceUtil.getCurrentProfile()
        if (profile.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        setShowWhenLocked(profile.showOnLock)

        enableEdgeToEdge()
        setContent {
            DancePlayerTheme() {
                MainScreen()
            }
        }
    }


    fun initMusic() {
        initMusicJob?.cancel()
        initMusicJob = lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                MusicLibrary.initialize(this@MainActivity)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val selectedPage = MainActivity.selectedPage
    val isInitializing by MusicLibrary.isInitializing
    val pageStack by MainActivity.pageStack
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
            pageStack.isNotEmpty() -> MainActivity.popLastPage()
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
                        MainActivity.pageStack.value = emptyList()
                    }
                )
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Text(pageStack.lastOrNull()?.getTitle() ?: MainActivity.pageTitles[selectedPage.intValue])
                    },
                    navigationIcon = {
                        if (pageStack.isNotEmpty()) {
                            IconButton(onClick = MainActivity::popLastPage) {
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

    if(MainActivity.popupOverlay.value) {
        Box (
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    MainActivity.popupOverlay.value = false
                    MainActivity.onDismissPopup()
                    MainActivity.onDismissPopup = {}
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
            MainActivity.pageTitles.forEachIndexed { idx, page ->
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