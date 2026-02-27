package com.example.danceplayer.model

import androidx.documentfile.provider.DocumentFile
import org.json.JSONObject
import kotlin.collections.iterator

data class Song(
    var file:Uri? = null,
    var tags: Map<String,Any>
) {
    companion object {

        const val _PATH: String = "path"
        const val _DATE: String = "last_modified"
        const val _TITLE: String = "title"
        const val _YEAR: String = "year"
        const val _ALBUM: String = "album"
        const val _ARTIST: String = "artist"
        const val _DANCE: String = "dance"
        const val _RATING: String = "rating"
        const val _TPM: String = "tpm"
        const val _DURATION: String = "duration"
        const val _PLAYING_AFTER: String = "playing_after"

        fun fromJSON(json: JSONObject, tags: Map<String, Tag>, onError: (String) -> Unit): Song? {
            val map = LinkedHashMap<String, Any>()
            val path = json.getString(_PATH)
            if (path == null) {
                onError("Song is missing path")
                return null
            }
            for (key in json.keys()) {
                val tagInfo = tags[key]
                if (tagInfo == null) continue
                try {
                    when (tagInfo.type) {
                        Tag.Type.STRING -> map.put(key, json.getString(key))
                        Tag.Type.INT -> map.put(key, json.getInt(key))
                        Tag.Type.FLOAT -> map.put(key, json.getDouble(key))
                        Tag.Type.RATING -> map.put(key, json.getInt(key))
                        Tag.Type.BOOL -> map.put(key, json.getBoolean(key))
                        Tag.Type.DATETIME -> map.put(key, json.getLong(key))
                        else -> {}
                    }
                } catch (e: Exception) {
                    onError("Invalid value in song $path for tag ${tagInfo.name}")
                    return null
                }
            }
            return Song(tags = map)
        }
    }

    fun getPath(): String {
        return tags[_PATH] as? String ?: ""
    }

    fun getTitle(): String {
        return tags[_TITLE] as? String ?: ""
    }

    fun getArtist(): String {
        return tags[_ARTIST] as? String ?: ""
    }

    fun getDuration(): Long {
        return MediaItem.fromUri(file).playbackProperties?.duration ?: 0L
    }

    fun getDance(): String {
        return tags[_DANCE] as? String ?: ""
    }


    fun asJSON(): JSONObject {
        val json = JSONObject()
        for ((key, value) in tags) {
            json.put(key, value)
        }
        return json
    }
}