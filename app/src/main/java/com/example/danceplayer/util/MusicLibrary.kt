// In einer neuen Datei: app/src/main/java/com/example/danceplayer/util/MusicFileUtil.kt

package com.example.danceplayer.util

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import com.example.danceplayer.model.Song
import com.example.danceplayer.model.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.net.toUri
import com.example.danceplayer.MainActivity
import com.example.danceplayer.ui.Main

object MusicLibrary {
    private val MUSIC_EXTENSIONS = setOf("mp3", "flac", "wav", "ogg", "aac", "m4a", "opus")

    val defaultTags = listOf<Tag>(
        Tag(Song._TITLE, Tag.Type.STRING),
        Tag(Song._ARTIST, Tag.Type.STRING),
        Tag(Song._DANCE, Tag.Type.STRING),
        Tag(Song._BPM, Tag.Type.FLOAT)
    )

    val calcTags = listOf(
        Tag(Song._DURATION, Tag.Type.TIME),
        Tag(Song._POSITION, Tag.Type.INT),
        Tag(Song._PLAYING_AFTER, Tag.Type.TIME)
    )

    val customTags = mutableStateOf<List<Tag>>(emptyList())

    val tags = derivedStateOf {
        defaultTags + customTags.value
    }

    val allTags = derivedStateOf {
        tags.value + calcTags
    }

    val tagMap = derivedStateOf {
        val map = mutableMapOf<String, Tag>()
        for (tag in allTags.value) {
            map.put(tag.name, tag)
        }
        map
    }

    val allSongs = mutableStateOf<List<Song>>(emptyList())

    val songs = derivedStateOf {
        allSongs.value.filter { song ->  song.file != null }
    }

    var songMap = derivedStateOf {
        val map = mutableMapOf<String, Song>()
        for (song in allSongs.value) {
            val path = song.tags[Song._PATH] as? String ?: continue
            map.put(path.lowercase(), song)
        }
        map
    }

    var isInitializing = mutableStateOf(false)



    suspend fun initialize(context: Context) {
        isInitializing.value = true
        val path = PreferenceUtil.getTagFile()
        val uri = path.toUri()
        loadTagFile(context, uri) { /* ignore error */ }
        val result = getMusicFiles(context)
        isInitializing.value = false
        Main.isInitialized = true
        //loadDuration()
        if(!result) {
            Main.selectedPage.intValue = 2
            return
        }
    }


    fun loadTagFile(context: Context, uri: Uri, onError: (String) -> Unit): Boolean {
        return try {
            val resolver = context.contentResolver
            val jsonString = resolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
            if (jsonString == null) {
                onError("Could not read file.")
                return false
            }


            if (!loadJSON(jsonString, onError)) {
                return false
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateViews() {
        allSongs.value = allSongs.value.toList()
        val temp = Player.currentSongState.value ?: return
        Player.currentSongState.value = null
        Player.currentSongState.value = temp
    }





    suspend fun getMusicFiles(context: Context): Boolean {
        val folderUri = PreferenceUtil.getCurrentProfile().folder
        if (folderUri == "") return false
        val treeUri = folderUri.toUri()
        val rootFolder = DocumentFile.fromTreeUri(context, treeUri) ?: return false
        val existingByPath = HashMap<String, Song>()
        allSongs.value = allSongs.value.filter { it.inFile } // keep only songs that were loaded from file
        allSongs.value.forEach { song ->
            song.file = null
            val path = song.tags[Song._PATH] as? String ?: return@forEach
           // existingByPath[path.lowercase()] = song
        }

        allSongs.value = searchMusicFiles(context, rootFolder.uri, "", existingByPath)
        withContext(Dispatchers.Main) {
            if (songs.value.isNotEmpty()) {
                Player.load(songs.value.subList(0, 1))
            }
        }

        allSongs.value = allSongs.value.toList()

        return true
    }

    suspend fun loadDuration() {
        for(song in allSongs.value) {
            song.getDuration() // cache duration for all songs
        }
        allSongs.value = allSongs.value.toList()
    }

    private fun searchMusicFiles(
        context: Context,
        folderUri: Uri,
        pathPrefix: String,
        existingByPath: Map<String, Song>
    ): ArrayList<Song> {
        val songList = ArrayList<Song>()
        val resolver = context.contentResolver
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        val treeDocumentId = DocumentsContract.getDocumentId(folderUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(folderUri, treeDocumentId)

        resolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

            while (cursor.moveToNext()) {
                if (idCol < 0 || nameCol < 0 || mimeCol < 0) continue
                val documentId = cursor.getString(idCol) ?: continue
                val name = cursor.getString(nameCol) ?: continue
                val mimeType = cursor.getString(mimeCol)
                val path = pathPrefix + name
                val pathLower = path.lowercase()

                if (DocumentsContract.Document.MIME_TYPE_DIR == mimeType) {
                    val subFolderUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, documentId)
                    songList.addAll(searchMusicFiles(context, subFolderUri, path + "/", existingByPath))
                    continue
                }

                if (isAudioFile(name)) {
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(folderUri, documentId)
                    var songInfo = songMap.value[pathLower]
                    if (songInfo == null) {
                        songInfo = Song(tags = mutableMapOf(Song._PATH to path))
                    }
                    songInfo.file = fileUri
                    songInfo.tags.put(Song._PATH, path) // make sure correct case is stored
                    songList.add(songInfo)
                }
            }
        }
        return songList
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

        context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
            out.write(content.toByteArray())
        }
    }

//    fun addTag(tag: Tag) {
//        customTags.value = customTags.value + tag
//        tagMap.value = tagMap.value + (tag.name to tag)
//    }
//
//    fun addSong(song: Song) {
//        allSongs.value = allSongs.value + song
//        val path = song.getPath()
//        if (path.isNotBlank())
//            songMap.value = songMap.value + (path.lowercase() to song)
//    }

    // remove all songs that don't have a file


    fun loadJSON(jsonString: String, onError: (String) -> Unit): Boolean {
        try {
            val json = JSONObject(jsonString)
            var arr = json.getJSONArray("tags")
            val tagList = ArrayList<Tag>()
            for (i in 0..arr.length() - 1) {
                tagList.add(Tag.fromJSON(arr.getJSONObject(i), onError) ?: return false)
            }
            customTags.value = tagList
            arr = json.getJSONArray("songs")
            val songList = ArrayList<Song>()
            for (i in 0..arr.length() - 1) {
                val song = Song.fromJSON(arr.getJSONObject(i), tagMap.value, onError)
                if(song == null) {
                    return false
                }
                songList.add(song)
            }
            allSongs.value = songList
        } catch (_: Exception) {
            onError("Error parsing content")
            return false
        }
        return true
    }

    fun asJSON(): JSONObject {
        val json = JSONObject()
        val tagArray = JSONArray()
        for (tag in customTags.value) tagArray.put(tag.asJSON())
        val songArray = JSONArray()
        for (song in allSongs.value) songArray.put(song.asJSON())
        json.put("tags", tagArray)
        json.put("songs", songArray)

        return json
    }


}