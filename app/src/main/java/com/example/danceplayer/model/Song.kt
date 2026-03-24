package com.example.danceplayer.model

import DateTimeUtil.formatDuration
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.documentfile.provider.DocumentFile
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import com.example.danceplayer.MainActivity
import com.example.danceplayer.ui.subpages.EditSong
import com.example.danceplayer.util.Player
import com.example.danceplayer.util.PreferenceUtil.getAppContextOrNull
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale
import kotlin.collections.iterator

data class Song(
    var file: Uri? = null,
    var tags: MutableMap<String,Any>,
    var duration: Long? = null,
    var inFile: Boolean = false
) {
    companion object {

        const val _PATH: String = "path"
        const val _TITLE: String = "title"
        const val _ARTIST: String = "artist"
        const val _DANCE: String = "dance"
        const val _RATING: String = "rating"
        const val _BPM: String = "bpm"
        const val _DURATION: String = "duration"
        const val _PLAYING_AFTER: String = "playing_after"
        const val _POSITION: String = "position"

        fun fromJSON(json: JSONObject, tags: Map<String, Tag>, onError: (String) -> Unit): Song? {
            val map = LinkedHashMap<String, Any>()
            val path = json.getString(_PATH)
            if (path == null) {
                onError("Song is missing path")
                return null
            }
            for (key in json.keys()) {
                val tagInfo = tags[key]
                if (tagInfo == null) {
                    map.put(key, json.get(key))
                    continue
                }
                try {
                    when (tagInfo.type) {
                        Tag.Type.STRING -> map.put(key, json.getString(key))
                        Tag.Type.INT -> map.put(key, json.getInt(key))
                        Tag.Type.FLOAT -> map.put(key, json.getDouble(key).toFloat())
                        Tag.Type.BOOL -> map.put(key, json.getBoolean(key))
                        Tag.Type.DATETIME, Tag.Type.DATE, Tag.Type.TIME -> map.put(key, json.getLong(key))
                        else -> {}
                    }
                } catch (e: Exception) {
                    onError("Invalid value in song $path for tag ${tagInfo.name}")
                    return null
                }
            }
            return Song(tags = map, inFile = true)
        }
    }

    fun getPath(): String {
        return tags[_PATH] as? String ?: ""
    }

    fun getTitle(): String {
        val title = tags[_TITLE] as? String ?: ""
        return title.ifBlank { getPath().substringAfterLast("/").substringBeforeLast(".") }
    }

    fun getArtist(): String {
        return tags[_ARTIST] as? String ?: ""
    }

    @OptIn(UnstableApi::class)
    fun getDuration(): Long {
        if (duration != null && duration!! > 0L) return duration!!

        val songUri = file ?: return 0L
        val context = getAppContextOrNull() ?: return 0L
        val retriever = MediaMetadataRetriever()
        duration = try {
            retriever.setDataSource(context, songUri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: RuntimeException) {
            Log.w("Song", "Could not read duration for uri=$songUri", e)
            0L
        } finally {
            retriever.release()
        }
        return duration!!
    }

    fun getDance(): String {
        return tags[_DANCE] as? String ?: "<EMPTY_DANCE>"
    }

    fun getTagValue(tag:Tag): Any {
        when(tag.name) {
            _TITLE -> return getTitle()
            _DURATION -> return formatDuration(getDuration())
            else -> {
                return when (tag.type) {
                    Tag.Type.DATE -> DateTimeFormatter
                        .ofLocalizedDate(FormatStyle.MEDIUM)
                        .localizedBy(Locale.getDefault())
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochMilli(tags[tag.name] as? Long ?: 0L))

                    Tag.Type.TIME -> formatDuration(tags[tag.name] as? Long ?: 0L)

                    Tag.Type.DATETIME -> DateTimeFormatter
                        .ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .localizedBy(Locale.getDefault())
                        .withZone(ZoneId.systemDefault())
                        .format(Instant.ofEpochMilli(tags[tag.name] as? Long ?: 0L))

                    else -> tags[tag.name] ?: tag.type.getDefault()
                }
            }
        }
    }

    fun asJSON(): JSONObject {
        val json = JSONObject()
        for ((key, value) in tags) {
            json.put(key, value)
        }
        return json
    }
}



data class ContextItem(
    val text:String,
    val onClick: (Song) -> Unit
) {
    companion object {
        val NEXT = ContextItem("Play next") { song ->
            Player.insertSong(song)
        }

        val APPEND = ContextItem("Add to queue") { song ->
            Player.appendSong(song)
        }

        val EDIT = ContextItem("Edit") { song ->
            MainActivity.addPage(EditSong(song))
        }
    }
}