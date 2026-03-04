// In einer neuen Datei: app/src/main/java/com/example/danceplayer/util/MusicFileUtil.kt

package com.example.danceplayer.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.danceplayer.model.Song
import com.example.danceplayer.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.net.toUri
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

object MusicLibrary {
    private val MUSIC_EXTENSIONS = setOf("mp3", "flac", "wav", "ogg", "aac", "m4a", "opus")

    var tags: MutableList<Tag> = mutableListOf()
    var songs: MutableList<Song> = mutableListOf()
    var tagMap: MutableMap<String, Tag> = LinkedHashMap()
    var songMap: MutableMap<String, Song> = LinkedHashMap()


    suspend fun initialize(context: Context) {
        val path = PreferenceUtil.getTagFile()
        if (path == "") {
            getDefaultInfo()
        } else {
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

    fun getAllTags(): List<Tag> {
        return tags + Tag(Song._DURATION, Tag.Type.DATETIME,6)
    }

    fun getAllTagsMap(): Map<String, Tag> {
        return tagMap + (Song._DURATION to Tag(Song._DURATION, Tag.Type.DATETIME,6))
    }

    private fun getDefaultInfo() {
        addTag(Tag(Song._DATE, Tag.Type.DATETIME,2));
        addTag(Tag(Song._TITLE, Tag.Type.STRING));
        addTag(Tag(Song._ARTIST, Tag.Type.STRING));
        addTag(Tag(Song._ALBUM, Tag.Type.STRING));
        addTag(Tag(Song._DANCE, Tag.Type.STRING));
        addTag(Tag(Song._YEAR, Tag.Type.INT));
        addTag(Tag(Song._TPM, Tag.Type.FLOAT,1));
    }

    suspend fun getMusicFiles(context: Context) {
        val folderUri = PreferenceUtil.getCurrentProfile().folder
        if (folderUri == "") return
        val uri = folderUri.toUri()
        val rootFolder = DocumentFile.fromTreeUri(context, uri) ?: return

        searchMusicFiles(rootFolder, "")
        filterSongs()
        withContext(Dispatchers.Main) {
            Player.load(songs.subList(0,1))
        }
    }

    private fun searchMusicFiles(folder: DocumentFile, pathPrefix: String) {
        folder.listFiles().forEach { file ->
            val path = pathPrefix + file.name
            val pathLower = path.lowercase()
            if(file.isDirectory) searchMusicFiles(file, path + "/") // Rekursiv in Unterordner
            if (file.isFile && isAudioFile(file.name)) {
                var songInfo = songMap[pathLower]
                if (songInfo == null) {
                    songInfo = Song(tags = mutableMapOf(Song._PATH to path))
                    addSong(songInfo)
                }
                songInfo?.file = file.uri
                songInfo?.tags?.put(Song._PATH, path) // make sure correct case is stored
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

    fun addTag(tag: Tag) {
        tags.add(tag)
        tagMap.put(tag.name, tag)
    }

    fun addSong(song: Song) {
        songs.add(song)
        val path = song.getPath()
        if (path.isNotBlank())
            songMap.put(path.lowercase(), song)
    }

    // remove all songs that don't have a file
    fun filterSongs() {
        val iterator = songs.iterator()
        while (iterator.hasNext()) {
            val song = iterator.next()
            if (song.file == null) {
                iterator.remove()
                songMap.remove(song.getPath())
            }
        }
    }

    fun loadJSON(jsonString: String, onError: (String) -> Unit): Boolean {
        tags = ArrayList<Tag>()
        songs = ArrayList<Song>()
        try {
            val json = JSONObject(jsonString)
            var arr = json.getJSONArray("tags")
            for (i in 0..arr.length() - 1) {
                addTag(Tag.fromJSON(arr.getJSONObject(i), onError) ?: return false)
            }
            arr = json.getJSONArray("songs")
            for (i in 0..arr.length() - 1) {
                val song = Song.fromJSON(arr.getJSONObject(i), tagMap, onError)
                if(song == null) {
                    return false
                }
                addSong(song)
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