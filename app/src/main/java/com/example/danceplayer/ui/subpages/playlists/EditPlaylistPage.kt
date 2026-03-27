package com.example.danceplayer.ui.subpages.playlists



class EditPlaylistPage(
    val playlist: Playlist
) : Fragment() {
    override fun getTitle(): String {
        return playlist.name
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val entries by remember { mutableStateOf(playlist.load(context)) }
        val layout = PreferenceUtil.getCurrentProfile().itemLayoutPlaylists
        val contextEntries = listOf(
            ContextItem.EDIT
        )

        Main {
            var ms = 0L
            for ((index, entry) in entries.withIndex()) {
                SongItem(
                    entry.song!!,
                    layout,
                    Modifier.fillMaxWidth(),
                    contextEntries,
                    index,
                    ms,
                    entry
                ) {
                    Player.load(entries, index)
                    Player.play(context)
                }

                if(index >= Player.currentIndex) {
                    ms += song.getDuration()
                }
            }
        }
}