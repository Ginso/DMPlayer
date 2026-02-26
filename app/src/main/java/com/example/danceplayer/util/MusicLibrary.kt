// In einer neuen Datei: app/src/main/java/com/example/danceplayer/util/MusicFileUtil.kt

package com.example.danceplayer.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object MusicLibrary {
    private val MUSIC_EXTENSIONS = setOf("mp3", "flac", "wav", "ogg", "aac", "m4a", "opus")

    suspend fun getMusicFiles(context: Context): List<DocumentFile> = withContext(Dispatchers.IO) {
        val folderUri = PreferenceUtil.getCurrentProfile().folder
        if (folderUri == null) return@withContext emptyList()
        val uri = Uri.parse(folderUri)
        val rootFolder = DocumentFile.fromTreeUri(context, uri) ?: return@withContext emptyList()
        val files = mutableListOf<DocumentFile>()
        searchMusicFiles(rootFolder, files)
        files
    }

    
    private fun searchMusicFiles(folder: DocumentFile, result: MutableList<DocumentFile>) {
        folder.listFiles().forEach { file ->
            when {
                file.isDirectory -> searchMusicFiles(file, result) // Rekursiv in Unterordner
                file.isFile && isAudioFile(file.name) -> result.add(file)
            }
        }
    }
    
    private fun isAudioFile(fileName: String?): Boolean {
        if (fileName == null) return false
        val extension = fileName.substringAfterLast(".").lowercase()
        return extension in MUSIC_EXTENSIONS
    }

    private fun getDefaultInfo(): AllInfo {
        val tags = ArrayList<TagInfo>()
        val songs = ArrayList<SongInfo>()
        
        tags.add(TagInfo(SongInfo._DURATION, TagInfo.Type.DATETIME,6));
        tags.add(TagInfo(SongInfo._PLAYING_AFTER, TagInfo.Type.DATETIME,5));
        tags.add(TagInfo(SongInfo._DATE, TagInfo.Type.DATETIME,2));
        tags.add(TagInfo(SongInfo._TITLE, TagInfo.Type.STRING));
        tags.add(TagInfo(SongInfo._ARTIST, TagInfo.Type.STRING));
        tags.add(TagInfo(SongInfo._ALBUM, TagInfo.Type.STRING));
        tags.add(TagInfo(SongInfo._DANCE, TagInfo.Type.STRING));
        tags.add(TagInfo(SongInfo._YEAR, TagInfo.Type.INT));
        tags.add(TagInfo(SongInfo._RATING, TagInfo.Type.RATING, 5));
        tags.add(TagInfo(SongInfo._TPM, TagInfo.Type.FLOAT,1));

        return AllInfo(tags, songs)
    }
}



data class TagInfo(
    var name:String = "",
    var type:Type = Type.STRING,
    var arg:Int = 0
) {
    enum class Type(val id: Int) {
        STRING(0),
        INT(1),
        FLOAT(2),
        RATING(3),
        BOOL(4),
        DATETIME(5),
        NONE(-1);

        companion object {
            fun fromInteger(x: Int): Type =
                entries.firstOrNull { it.id == x } ?: NONE


        }
        fun getText(arg: Int):String {
            when (this) {
                STRING -> return "Text"
                INT -> return "Integer"
                FLOAT -> return "Decimal"
                RATING -> return "Rating"
                BOOL -> return "Yes/No"
                DATETIME -> {
                    if (arg < 5) return "Date"
                    return "Time"
                }
                NONE -> return ""
            }
        }

    }
    companion object {

        fun fromJSON(json: JSONObject): TagInfo {
            return try {
                TagInfo(
                    name = json.getString("name"),
                    type = TagInfo.Type.fromInteger(json.getInt("type")),
                    arg = json.getInt("arg")
                )
            } catch (e: Exception) {
                TagInfo()
            }
        }
    }

    fun asJSON(): JSONObject {
        val json = JSONObject()
        json.put("name", name)
        json.put("type", type)
        json.put("arg", arg)
        return json
    }
}

data class SongInfo(
    var tags: Map<String,Any>
) {
    companion object {

        const val _DATE: String = "last_modified"
        const val _DURATION: String = "duration"
        const val _PLAYING_AFTER: String = "playing_after"
        const val _TITLE: String = "title"
        const val _YEAR: String = "year"
        const val _ALBUM: String = "album"
        const val _ARTIST: String = "artist"
        const val _DANCE: String = "dance"
        const val _RATING: String = "rating"
        const val _TPM: String = "tpm"
        fun fromJSON(json: JSONObject): SongInfo {
            val map = LinkedHashMap<String, Any>()
            return try {
                for (key in json.keys()) {
                    map.put(key, json.get(key))
                }
                SongInfo(map)
            } catch (e: Exception) {
                SongInfo(map)
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

data class AllInfo(
    var tags: List<TagInfo>,
    var songs: List<SongInfo>
) {
    companion object {

        fun fromJSON(json: JSONObject): AllInfo? {
            val tags = ArrayList<TagInfo>()
            val songs = ArrayList<SongInfo>()
            try {
                var arr = json.getJSONArray("tags")
                for(i in 0.. arr.length()-1) {
                    tags.add(TagInfo.fromJSON(arr.getJSONObject(i)))
                }
                arr = json.getJSONArray("songs")
                for(i in 0.. arr.length()-1) {
                    songs.add(SongInfo.fromJSON(arr.getJSONObject(i)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
            return AllInfo(tags = tags, songs = songs)
        }
    }

    fun asJSON(): JSONObject {
        val json = JSONObject()
        val tagArray = JSONArray()
        for (tag in tags) tagArray.put(tag.asJSON())
        val songArray = JSONArray()
        for (tag in songs) songArray.put(tag.asJSON())
        json.put("tags", tagArray)
        json.put("songs", songArray)

        return json
    }
}