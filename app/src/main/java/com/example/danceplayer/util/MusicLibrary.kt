// In einer neuen Datei: app/src/main/java/com/example/danceplayer/util/MusicFileUtil.kt

package com.example.danceplayer.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MusicLibrary {
    private val MUSIC_EXTENSIONS = setOf("mp3", "flac", "wav", "ogg", "aac", "m4a", "opus")

    suspend fun getMusicFiles(context: Context): List<DocumentFile> = withContext(Dispatchers.IO) {
        val folderUri = PreferenceUtil.getProfile().folder
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
}