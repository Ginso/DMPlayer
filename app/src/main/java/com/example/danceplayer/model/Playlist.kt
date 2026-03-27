package com.example.danceplayer.model


data class Playlist(
    var name: String,
    var file: Uri? = null,
    var songs: Int = 0
) {
    fun load(context: Context): List<PlaylistEntry> {
        val entries = ArrayList<PlaylistEntry>()
        if (file != null) {
            val resolver = context.contentResolver
            val contentString = resolver.openInputStream(file)?.bufferedReader().use { it?.readText() }
            val lines = contentString?.lines() ?: emptyList()
            val entry = PlaylistEntry()
            for (line in lines) {
                if(line.startsWith("#")) {
                    if (line.contains("start-time=")) {
                        val startTime = line.substringAfter("start-time=").toIntOrNull()
                        entry.startTime = startTime
                    } else if (line.contains("stop-time=")) {
                        val endTime = line.substringAfter("stop-time=").toIntOrNull()
                        entry.endTime = endTime
                    } else if (line.contains("speed=")) {
                        val speed = line.substringAfter("speed=").toFloatOrNull()
                        entry.speed = speed
                    }
                } else if (line.isNotBlank() && !line.startsWith("#")) {
                    val uri = Uri.parse(line)
                    // todo check uri format
                    val song = MusicLibrary.songMap[uri.toString()]
                    if (song != null) {
                        entry.song = song
                        entries.add(entry.copy())
                    }
                    entry = PlaylistEntry()
                }
            }
        }
        songs = entries.size
        return entries
    }
}

data class PlaylistEntry(
    var name: String = "",
    var song: Song? = null,
    var startTime: Int? = null,
    var endTime: Int? = null,
    var speed: Float? = null
)