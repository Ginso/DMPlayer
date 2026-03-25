package com.example.danceplayer.service

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.danceplayer.R
import com.example.danceplayer.util.Player as AppPlayer

class PlaybackService : MediaSessionService() {

    companion object {
        private const val CHANNEL_ID = "playback"
    }

    override fun onCreate() {
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(CHANNEL_ID)
                .setChannelName(R.string.app_name)
                .setNotificationId(1001)
                .build()
        )
        super.onCreate()
        AppPlayer.initialize(applicationContext)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return AppPlayer.getMediaSession()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = AppPlayer.getExoPlayer()
        val shouldStop = player == null ||
            (!player.isPlaying && player.playbackState == Player.STATE_ENDED)

        if (shouldStop) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        AppPlayer.release()
        super.onDestroy()
    }
}