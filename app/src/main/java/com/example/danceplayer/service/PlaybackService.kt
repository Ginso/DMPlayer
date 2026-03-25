package com.example.danceplayer.service

import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaSession
import com.example.danceplayer.util.Player as AppPlayer

class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val player = AppPlayer.getExoPlayer()!!

        mediaSession = MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}