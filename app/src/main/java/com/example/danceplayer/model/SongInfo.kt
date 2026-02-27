package com.example.danceplayer.model

import androidx.documentfile.provider.DocumentFile
import org.json.JSONObject
import kotlin.collections.iterator

data class SongInfo(
    var file:DocumentFile? = null,
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

        fun fromJSON(json: JSONObject, tags: Map<String, TagInfo>, onError: (String) -> Unit): SongInfo? {
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
                        TagInfo.Type.STRING -> map.put(key, json.getString(key))
                        TagInfo.Type.INT -> map.put(key, json.getInt(key))
                        TagInfo.Type.FLOAT -> map.put(key, json.getDouble(key))
                        TagInfo.Type.RATING -> map.put(key, json.getInt(key))
                        TagInfo.Type.BOOL -> map.put(key, json.getBoolean(key))
                        TagInfo.Type.DATETIME -> map.put(key, json.getLong(key))
                        else -> {}
                    }
                } catch (e: Exception) {
                    onError("Invalid value in song $path for tag ${tagInfo.name}")
                    return null
                }
            }
            return SongInfo(tags = map)
        }
    }

    fun getPath(): String {
        return tags[_PATH] as? String ?: ""
    }


    fun asJSON(): JSONObject {
        val json = JSONObject()
        for ((key, value) in tags) {
            json.put(key, value)
        }
        return json
    }
}