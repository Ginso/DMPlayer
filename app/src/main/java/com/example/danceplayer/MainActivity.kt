package com.example.danceplayer

// additional layout/imports used in BottomBar
import android.Manifest
import android.os.Bundle
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.danceplayer.ui.Main
import com.example.danceplayer.ui.MainScreen
import com.example.danceplayer.ui.theme.DancePlayerTheme
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.Player
import com.example.danceplayer.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.danceplayer.service.PlaybackService


class MainActivity : ComponentActivity() {
    companion object {

        private var controller: MediaController? = null


    }
    private var initMusicJob: kotlinx.coroutines.Job? = null
    private val requestReadAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                initMusic()
            }
        }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val sessionToken = SessionToken(
            this,
            ComponentName(this, PlaybackService::class.java)
        )

        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                controller = controllerFuture.get()
            },
            ContextCompat.getMainExecutor(this)
        )

        if (!Main.isInitialized) {
            // Always initialize prefs/player; music scan depends on read permission.
            initMusicJob = lifecycleScope.launch {
                PreferenceUtil.initialize(this@MainActivity)
                Player.initialize(this@MainActivity)

                val hasReadAudio =
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.READ_MEDIA_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                if (hasReadAudio) {
                    withContext(Dispatchers.IO) {
                        MusicLibrary.initialize(this@MainActivity)
                    }
                } else {
                    requestReadAudioPermission.launch(Manifest.permission.READ_MEDIA_AUDIO)
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

    override fun onDestroy() {
        controller?.release()
        controller = null
        super.onDestroy()
    }

    fun initMusic() {
        initMusicJob?.cancel()
        initMusicJob = lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                MusicLibrary.initialize(this@MainActivity)
            }
        }
    }

}