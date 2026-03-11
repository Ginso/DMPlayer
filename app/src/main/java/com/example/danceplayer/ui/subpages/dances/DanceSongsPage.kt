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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.danceplayer.model.Song
import com.example.danceplayer.model.Tag
import com.example.danceplayer.ui.Fragment
import com.example.danceplayer.ui.subpages.settings.HeaderCell
import com.example.danceplayer.util.MusicLibrary
import com.example.danceplayer.util.Player
import com.example.danceplayer.util.PreferenceUtil
import com.example.danceplayer.util.SongItem
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanceSongsPage(dance: String, onBack: () -> Unit) {
    val profile = PreferenceUtil.getCurrentProfile()
    val songs = remember { mutableStateOf(MusicLibrary.songs.filter { it.getDance() == dance }) }
    val sorter = remember {mutableStateOf("")}
    val filterOptions = remember { mutableStateOf(profile.filterOptions) }
    val itemLayout = profile.itemLayoutBrowser
    MusicLibrary.hooks.add { songs.value = MusicLibrary.songs.filter { it.getDance() == dance } }
    Fragment(dance, onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
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
                        val tag = MusicLibrary.getAllTagsMap().get(tagName)
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
                        HeaderCell(o, tag, val1, val2, onValueChange = { newVal ->
                            if(filter) {
                                o.put("value1", newVal)
                                filterOptions.value = JSONArray(filterOptions.value.toString())
                                applyFilters(songs, filterOptions.value, sorter.value)
                            } else {
                                sorter.value = when(newVal) {
                                    2 -> "-$tagName"
                                    else -> tagName
                                }
                                applySorting(songs, tag, newVal as Int)
                            }
                        }, onValue2Change = { newVal ->
                            o.put("value2", newVal)
                            filterOptions.value = JSONArray(filterOptions.value.toString())
                            applyFilters(songs, filterOptions.value, sorter.value)
                        })
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for((index, song) in songs.value.withIndex()) {
                SongItem(
                    song, 
                    itemLayout, 
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            Player.load(songs.value, index)
                        }
                )
            }
        }
    }
}

fun applySorting(songs: MutableState<List<Song>>, tag: Tag, sortValue: Int) {
    val sorted = when(sortValue) {
        1 -> songs.value.sortedBy { it.getTagValue(tag) as? Comparable<Any> }
        2 -> songs.value.sortedByDescending { it.getTagValue(tag) as? Comparable<Any> }
        else -> songs.value
    }
    songs.value = sorted
}

fun applyFilters(songs: MutableState<List<Song>>, filterOptions: JSONArray, sorter: String) {
    var filtered:List<Song> = MusicLibrary.songs
    val tagMap = MusicLibrary.getAllTagsMap()
    for(i in 0 until filterOptions.length()) {
        val row = filterOptions.getJSONArray(i)
        for(j in 0 until row.length()) {
            val o = row.getJSONObject(j)
            if(o.getBoolean("filter")) {
                val tagName = o.getString("tag")
                val tag = tagMap.get(tagName) ?: continue
                val value1 = o.opt("value1")
                val value2 = o.opt("value2")
                filtered = filtered.filter { song ->
                    val songValue = song.getTagValue(tag)
                    if(songValue == null) return@filter false
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
                            val v1 = (value1 as? Number)?.toDouble() ?: -Double.MAX_VALUE
                            val v2 = (value2 as? Number)?.toDouble() ?: Double.MAX_VALUE
                            when(o.getJSONArray("type").getInt(1)) {
                                0 -> numValue <= v1
                                1 -> numValue >= v1
                                2 -> numValue < v1
                                3 -> numValue > v1
                                4 -> v1 < numValue && numValue < v2
                                5 -> v1 <= numValue && numValue <= v2
                                6 -> v1 <= numValue && numValue < v2
                                7 -> v1 < numValue && numValue <= v2
                                else -> true
                            }
                        }
                    }
                }
            }
        }
    }
    songs.value = filtered
    val tag = tagMap.get(sorter.trimStart('-'))
    if(tag != null) applySorting(songs, tag, if(sorter.startsWith("-")) 2 else 1)
}