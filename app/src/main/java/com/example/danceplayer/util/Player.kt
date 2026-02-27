package com.example.danceplayer.util

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.ExoPlayer
import com.example.danceplayer.model.Song

object Player {
    private var exoPlayer: ExoPlayer? = null
    
    var isPlaying: Boolean = false
        private set
    var position: Long = 0L
        private set
    var speed: Float = 1.0f
        private set
    var playlist: List<Song> = emptyList()
        private set
    var currentIndex: Int = 0
        private set

    fun initialize(context: Context) {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
        }
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }

    fun getCurrentSong(): Song? {
        return if (currentIndex in playlist.indices) {
            playlist[currentIndex]
        } else null
    }

    fun play() {
        isPlaying = true
        exoPlayer?.play()
    }

    fun pause() {
        isPlaying = false
        exoPlayer?.pause()
    }

    fun load(songs: List<Song>, index: Int = 0) {
        playlist = songs
        currentIndex = index
        val mediaItems = songs.map { song ->
            MediaItem.fromUri(song.file!!)
        }
        exoPlayer?.setMediaItems(mediaItems, index, 0L)
        seekTo(0L)
    }

    fun seekTo(newPosition: Long) {
        position = newPosition
        exoPlayer?.seekTo(newPosition)
    }

    fun setSpeed(newSpeed: Float) {
        speed = newSpeed
        exoPlayer?.setPlaybackParameters(PlaybackParameters(newSpeed))
    }

    fun next() {
        if (currentIndex < playlist.size - 1) {
            currentIndex++
            position = 0L
            exoPlayer?.seekToNextMediaItem()
        }
    }

    fun previous() {
        if (position > 3000) {
            position = 0L
            exoPlayer?.seekTo(0L)
        } else if (currentIndex > 0) {
            currentIndex--
            position = 0L
            exoPlayer?.seekToPreviousMediaItem()
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
        }
    }
}