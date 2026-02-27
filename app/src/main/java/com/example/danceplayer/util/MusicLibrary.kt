// In einer neuen Datei: app/src/main/java/com/example/danceplayer/util/MusicFileUtil.kt

package com.example.danceplayer.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.danceplayer.model.SongInfo
import com.example.danceplayer.model.TagInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.net.toUri

object MusicLibrary {
    private val MUSIC_EXTENSIONS = setOf("mp3", "flac", "wav", "ogg", "aac", "m4a", "opus")

    var tags: List<TagInfo> = emptyList()
    var songs: List<SongInfo> = emptyList()
    var tagMap: Map<String, TagInfo> = LinkedHashMap()
    var songMap: Map<String, SongInfo> = LinkedHashMap()


    suspend fun initialize(context: Context) {
        val path = PreferenceUtil.getTagFile()
        if (path != "") {
            val uri = path.toUri()
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


            if (!loadJSON(jsonString, onError)) {
                return@withContext false
            }
            true
        }
    }

    fun getMusicFiles(context: Context) {
        val folderUri = PreferenceUtil.getCurrentProfile().folder
        if (folderUri == "") return
        val uri = folderUri.toUri()
        val rootFolder = DocumentFile.fromTreeUri(context, uri) ?: return
        searchMusicFiles(rootFolder, "")
        filterSongs()
    }


    private fun searchMusicFiles(folder: DocumentFile, pathPrefix: String) {
        folder.listFiles().forEach { file ->
            if(file.isDirectory) searchMusicFiles(file,pathPrefix + file.name + "/") // Rekursiv in Unterordner
            if (file.isFile && isAudioFile(file.name)) {
                var songInfo = songMap[pathPrefix + file.name]
                if (songInfo == null) {
                    songInfo = SongInfo(tags = mapOf(SongInfo._PATH to pathPrefix + file.name))
                    addSong(songInfo)
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


    fun save(context: Context) {
        val content = asJSON().toString()
        val path = PreferenceUtil.getTagFile()
        val uri = path.toUri()
        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(content.toByteArray())
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

    fun loadJSON(jsonString: String, onError: (String) -> Unit): Boolean {
        val tags = ArrayList<TagInfo>()
        val songs = ArrayList<SongInfo>()
        try {
            val json = JSONObject(jsonString)
            var arr = json.getJSONArray("tags")
            for (i in 0..arr.length() - 1) {
                tags.add(TagInfo.fromJSON(arr.getJSONObject(i), onError) ?: return false)
            }
            arr = json.getJSONArray("songs")
            for (i in 0..arr.length() - 1) {
                val song = SongInfo.fromJSON(arr.getJSONObject(i), tagMap, onError)
                if(song == null) {
                    return false
                }
                songs.add(song)
            }
        } catch (_: Exception) {
            onError("Error parsing content")
            return false
        }
        this.tags = tags
        this.songs = songs
        return true
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