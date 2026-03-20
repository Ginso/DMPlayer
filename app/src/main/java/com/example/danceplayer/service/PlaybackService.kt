package com.example.danceplayer.service

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.danceplayer.util.Player as AppPlayer

class PlaybackService : MediaSessionService() {

    override fun onCreate() {
        super.onCreate()
        AppPlayer.initialize(applicationContext)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return AppPlayer.getMediaSession()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = AppPlayer.getExoPlayer()
        if (player == null || !player.playWhenReady || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        AppPlayer.release()
        super.onDestroy()
    }
}
