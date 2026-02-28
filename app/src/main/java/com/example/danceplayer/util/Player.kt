package com.example.danceplayer.util

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player as Media3Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.danceplayer.model.Song

import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Player {
    private var exoPlayer: ExoPlayer? = null

    // expose compose-friendly state holders
    val isPlayingState = mutableStateOf(false)
    val positionState = mutableStateOf(0L)
    val speedState = mutableStateOf(1.0f)
    val currentSongState = mutableStateOf<Song?>(null)

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var positionJob: Job? = null

    // internal backing fields for playlist and index (no state needed)
    private var playlist: List<Song> = emptyList()
    private var currentIndex: Int = 0


    @OptIn(UnstableApi::class)
    fun initialize(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
            exoPlayer?.addListener(object : Media3Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    isPlayingState.value = isPlaying
                    if (isPlaying) startPositionUpdater() else stopPositionUpdater()
                }
                override fun onPlaybackStateChanged(state: Int) {
                    positionState.value = exoPlayer?.currentPosition ?: 0L
                }
                override fun onPositionDiscontinuity(reason: Int) {
                    positionState.value = exoPlayer?.currentPosition ?: 0L
                }
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("Player", "Playback error", error)
                    // ggf. Toast o.ä. anzeigen
                }
            })
        }
    }

    fun load(songs: List<Song>, index: Int = 0) {
        playlist = songs
        currentIndex = index
        val mediaItems = songs.map { song -> MediaItem.fromUri(song.file!!) }
        exoPlayer?.setMediaItems(mediaItems, index, 0L)
        exoPlayer?.prepare()                 // <<< wichtig
        seekTo(0L)
        updateCurrentSong()
    }

    fun release() {
        positionJob?.cancel()
        scope.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }

    private fun startPositionUpdater() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isPlayingState.value) {
                positionState.value = exoPlayer?.currentPosition ?: 0L
                delay(1000)
            }
        }
    }

    private fun stopPositionUpdater() {
        positionJob?.cancel()
    }

    private fun updateCurrentSong() {
        currentSongState.value = if (currentIndex in playlist.indices) playlist[currentIndex] else null
    }

    fun getCurrentSong(): Song? = currentSongState.value

    fun play() {
        exoPlayer?.play()
        isPlayingState.value = true
        startPositionUpdater()
    }

    fun pause() {
        exoPlayer?.pause()
        isPlayingState.value = false
        stopPositionUpdater()
    }

    fun load(songs: List<Song>, index: Int = 0) {
        playlist = songs
        currentIndex = index
        val mediaItems = songs.map { song ->
            MediaItem.fromUri(song.file!!)
        }
        exoPlayer?.setMediaItems(mediaItems, index, 0L)
        seekTo(0L)
        updateCurrentSong()
    }

    fun seekTo(newPosition: Long) {
        exoPlayer?.seekTo(newPosition)
        positionState.value = newPosition
    }

    fun setSpeed(newSpeed: Float) {
        speedState.value = newSpeed
        exoPlayer?.setPlaybackParameters(PlaybackParameters(newSpeed))
    }

    fun next() {
        if (currentIndex < playlist.size - 1) {
            currentIndex++
            positionState.value = 0L
            exoPlayer?.seekToNextMediaItem()
            updateCurrentSong()
        }
    }

    fun previous() {
        val currentPos = positionState.value
        if (currentPos > 3000) {
            seekTo(0L)
        } else if (currentIndex > 0) {
            currentIndex--
            positionState.value = 0L
            exoPlayer?.seekToPreviousMediaItem()
            updateCurrentSong()
        }
    }

    fun removeFromPlaylist(index: Int) {
        if (index in playlist.indices) {
            val mutableList = playlist.toMutableList()
            mutableList.removeAt(index)
            playlist = mutableList
            if (index == currentIndex) {
                next()
                return
            }
            if (currentIndex > index) {
                currentIndex = maxOf(0, currentIndex - 1)
            }
            exoPlayer?.removeMediaItem(index)
            updateCurrentSong()
        }
    }
}