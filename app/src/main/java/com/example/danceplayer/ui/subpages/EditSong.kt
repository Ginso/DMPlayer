package com.example.danceplayer.ui.subpages

import androidx.compose.runtime.Composable
import com.example.danceplayer.model.Song
import com.example.danceplayer.ui.Fragment

class EditSong(
    val song: Song
) : Fragment() {

    override fun getTitle(): String {
        return "Edit Tags"
    }

    @Composable
    override fun Content() {

    }
}