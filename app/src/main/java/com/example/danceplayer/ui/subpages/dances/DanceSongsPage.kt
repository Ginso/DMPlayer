package com.example.danceplayer.ui.subpages.dances

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.danceplayer.model.ContextItem
import com.example.danceplayer.model.Song
import com.example.danceplayer.model.Tag
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.ui.subpages.settings.FilterPage
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.Player
import com.example.danceplayer.util.PreferenceUtil
import com.example.danceplayer.util.SongItem
import org.json.JSONArray

class DanceSongsPage(
    val dance: String
) : Fragment() {



    override fun getTitle(): String {
        return dance
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val profile = PreferenceUtil.getCurrentProfile()
        val itemLayout = profile.itemLayoutBrowser
        val sorter = remember {mutableStateOf("")}
        val filterOptions = remember { mutableStateOf(profile.filterOptions) }
        val filteredSongs = remember {
            derivedStateOf {
                val songs = MusicLibrary.songs.value.filter { it.getDance() == dance }
                applyFilters(songs, filterOptions.value, sorter.value)
            }
        }
        val contextEntries = listOf(
            ContextItem.NEXT,
            ContextItem.APPEND,
            ContextItem.EDIT
        )
        Main {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                for(i in 0 until filterOptions.value.length()) {
                    val row = filterOptions.value.getJSONArray(i)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                            // todo min height

                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for(j in 0 until row.length()) {
                            val o = row.getJSONObject(j)
                            val tagName = o.getString("tag")
                            val tag = MusicLibrary.allTagsMap.value[tagName]
                            if(tag == null) {
                                Text("INVALID")
                                continue
                            }
                            var val1:Any? = null
                            var val2:Any? = null
                            val filter = o.getBoolean("filter")
                            if(filter) {
                                val1 = o.opt("value1")
                                val2 = o.opt("value2")
                            } else {
                                val1 = if(sorter.value == tagName) 1 else if(sorter.value == "-$tagName") 2 else 0
                            }
                            FilterPage.HeaderCell(o, tag, val1, val2, onValueChange = { newVal ->
                                if(filter) {
                                    o.put("value1", newVal)
                                    filterOptions.value = JSONArray(filterOptions.value.toString())
                                } else {
                                    sorter.value = when(newVal) {
                                        2 -> "-$tagName"
                                        else -> tagName
                                    }
                                }
                            }, onValue2Change = { newVal ->
                                o.put("value2", newVal)
                                filterOptions.value = JSONArray(filterOptions.value.toString())
                            })
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for((index, song) in filteredSongs.value.withIndex()) {
                    SongItem(
                        song, 
                        itemLayout, 
                        Modifier
                            .fillMaxWidth(),
                        contextEntries
                    ) {
                        Player.load(filteredSongs.value, index)
                        Player.play()
                    }
                }
            }
        }
    }

    fun applySorting(songs: List<Song>, tag: Tag, sortValue: Int):List<Song> {
        val sorted = when(sortValue) {
            1 -> songs.sortedBy { it.getTagValue(tag) as? Comparable<Any> }
            2 -> songs.sortedByDescending { it.getTagValue(tag) as? Comparable<Any> }
            else -> songs
        }
        return sorted
    }

    fun applyFilters(songs: List<Song>, filterOptions: JSONArray, sorter: String):List<Song> {
        var filtered = songs
        val tagMap = MusicLibrary.allTagsMap.value
        for(i in 0 until filterOptions.length()) {
            val row = filterOptions.getJSONArray(i)
            for(j in 0 until row.length()) {
                val o = row.getJSONObject(j)
                if(o.getBoolean("filter")) {
                    val tagName = o.getString("tag")
                    val tag = tagMap.get(tagName) ?: continue
                    val value1 = o.opt("value1")
                    val value2 = o.opt("value2")
                    if(value1 == null && value2 == null) continue
                    filtered = filtered.filter { song ->
                        val songValue = song.getTagValue(tag)
                        when(tag.type) {
                            Tag.Type.STRING -> {
                                val strValue = songValue as? String ?: return@filter false
                                strValue.contains(value1.toString(), ignoreCase = true)
                            }
                            Tag.Type.BOOL -> {
                                if(value1 == 0) return@filter true
                                val boolValue = songValue as? Boolean ?: return@filter false
                                boolValue == (value1 == 1)
                            }
                            else -> {
                                val numValue = (songValue as? Number)?.toDouble() ?: return@filter false
                                val v1orMin = (value1 as? Number)?.toDouble() ?: -Double.MAX_VALUE
                                val v1orMax = (value1 as? Number)?.toDouble() ?: Double.MAX_VALUE
                                val v2 = (value2 as? Number)?.toDouble() ?: Double.MAX_VALUE
                                when(o.getJSONArray("type").getInt(1)) {
                                    0 -> numValue <= v1orMax
                                    1 -> numValue >= v1orMin
                                    2 -> numValue < v1orMax
                                    3 -> numValue > v1orMin
                                    4 -> v1orMin < numValue && numValue < v2
                                    5 -> v1orMin <= numValue && numValue <= v2
                                    6 -> v1orMin <= numValue && numValue < v2
                                    7 -> v1orMin < numValue && numValue <= v2
                                    else -> true
                                }
                            }
                        }
                    }
                }
            }
        }
        val tag = tagMap.get(sorter.trimStart('-'))
        if(tag != null) filtered = applySorting(filtered, tag, if(sorter.startsWith("-")) 2 else 1)
        return filtered
    }
}