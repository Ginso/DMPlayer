package com.example.danceplayer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.danceplayer.MainActivity
import com.example.danceplayer.R
import com.example.danceplayer.util.Player as AppPlayer

class PlaybackService : MediaSessionService() {

    companion object {
        private const val CHANNEL_ID = "playback"
        private const val NOTIFICATION_ID = 1001
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(CHANNEL_ID)
                .setChannelName(R.string.app_name)
                .setNotificationId(NOTIFICATION_ID)
                .build()
        )
        super.onCreate()
        AppPlayer.initialize(applicationContext)
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        super.onStartCommand(intent, flags, startId)
//        // Ensure foreground immediately to avoid app-idle kill on some OEMs.
//        startForeground(NOTIFICATION_ID, buildBootstrapNotification())
//        return START_STICKY
//    }
//
//    private fun buildBootstrapNotification(): Notification {
//        val launchIntent = Intent(this, MainActivity::class.java)
//        val contentIntent = PendingIntent.getActivity(
//            this,
//            0,
//            launchIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        return NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_play)
//            .setContentTitle(getString(R.string.app_name))
//            .setContentText("Wiedergabe wird vorbereitet…")
//            .setContentIntent(contentIntent)
//            .setOngoing(true)
//            .setOnlyAlertOnce(true)
//            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
//            .build()
//    }


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