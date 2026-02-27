// In einer neuen Datei: app/src/main/java/com/example/danceplayer/util/MusicFileUtil.kt

package com.example.danceplayer.util

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object MusicLibrary {
    private val MUSIC_EXTENSIONS = setOf("mp3", "flac", "wav", "ogg", "aac", "m4a", "opus")

    public var allInfo: AllInfo = getDefaultInfo()

    suspend fun initialize(context: Context) {
        val path = PreferenceUtil.getTagFile()
        if (path != null) {
            val uri = Uri.parse(path)
            loadTagFile(context, uri) { /* ignore error */ }
        }
        getMusicFiles(context)
    }

    suspend fun loadTagFile(context: Context, uri: Uri, onError: (String) -> Unit): Boolean {
        return withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val jsonString = resolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
            if (jsonString == null) {
                onError("Could not read file.")
                return@withContext false
            }

            val info = AllInfo.fromJSON(jsonString, onError)
            if (info == null) {
                return@withContext false
            }
            allInfo = info
            true
        }
    }

    suspend fun getMusicFiles(context: Context) {
        val folderUri = PreferenceUtil.getCurrentProfile().folder
        if (folderUri == null) return
        val uri = Uri.parse(folderUri)
        val rootFolder = DocumentFile.fromTreeUri(context, uri) ?: return
        searchMusicFiles(rootFolder, "")
        allInfo.filterSongs()
    }

    
    private fun searchMusicFiles(folder: DocumentFile, pathPrefix: String) {
        folder.listFiles().forEach { file ->
            when {
                file.isDirectory -> searchMusicFiles(file, result, pathPrefix + file.name + "/") // Rekursiv in Unterordner
                if(file.isFile && isAudioFile(file.name)) {
                    var songInfo = allInfo.songMap[pathPrefix + file.name]
                    if (songInfo == null) {
                        songInfo = SongInfo(tags = mapOf(SongInfo._PATH to pathPrefix + file.name))
                        allInfo.addSong(songInfo)
                    }
                    songInfo.file = file
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

    private fun save(context: Context) {
        val content = allInfo.asJSON().toString()
        val path = PreferenceUtil.getTagFile()
        val uri = Uri.parse(path)
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(content.toByteArray())
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

        fun fromJSON(json: JSONObject, onError: (String) -> Unit): TagInfo? {
            return try {
                TagInfo(
                    name = json.getString("name"),
                    type = TagInfo.Type.fromInteger(json.getInt("type")),
                    arg = json.getInt("arg")
                )
            } catch (e: Exception) {
                onError("Error parsing tag info: ${json.toString()}")
                return null
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
            var path = json.optString(_PATH, null)
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
            return SongInfo(map)
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

data class AllInfo(
    var tags: List<TagInfo>,
    var songs: List<SongInfo>,
    var tagMap: Map<String, TagInfo> = LinkedHashMap()
    var songMap: Map<String, SongInfo> = LinkedHashMap()
) {
    companion object {

        fun fromJSON(jsonString: String, onError: (String) -> Unit): AllInfo? {
            val tags = ArrayList<TagInfo>()
            val songs = ArrayList<SongInfo>()
            try {
                val json = JSONObject(jsonString)
                var arr = json.getJSONArray("tags")
                for(i in 0.. arr.length()-1) {
                    tags.add(TagInfo.fromJSON(arr.getJSONObject(i), onError) ?: return null)
                }
                arr = json.getJSONArray("songs")
                for(i in 0.. arr.length()-1) {
                    songs.add(SongInfo.fromJSON(arr.getJSONObject(i), tagMap, onError) ?: return null)
                }
            } catch (e: Exception) {
                onError("Error parsing content")
                return null
            }
            return AllInfo(tags = tags, songs = songs)
        }
    }

    fun addTag(tag: TagInfo) {
        tags = tags + tag
        tagMap = tagMap + (tag.name to tag)
    }

    fun addSong(song: SongInfo) {
        songs = songs + song
        val path = song.getPath()
        if (path.isNotBlank())
            songMap = songMap + (path to song)
    }

    fun filterSongs() {
        songs = songs.filter { it.getPath().isNotBlank() }
        songMap = songMap.filter { (_, song) -> song.getPath().isNotBlank() }
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