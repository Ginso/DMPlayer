package com.example.danceplayer.ui.subpages.dances

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DanceSongsPage(dance: String, onBack: () -> Unit) {
    val songs = remember { mutableStateOf(MusicLibrary.songs.filter { it.getDance() == dance }) }
    var sorter = remember {mutableStateOf("")}
    var filterOptions = remember { mutableStateOf(PreferenceUtil.getCurrentProfile().filterOptions) }
    Fragment(dance, onBack) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            for(i in 0 until filterOptions.value.length()) {
                val row = filterOptions.value.getJSONArray(i)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedRow.value = i
                        }
                        .padding(4.dp)
                        // todo min height
                        .background(if(selectedRow.value == i) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent),

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
                        var val1 = null
                        var val2 = null
                        val filter = o.getBoolean("filter")
                        if(filter) {
                            val1 = o.opt("value1")
                            val2 = o.opt("value2")
                        } else if(o.getBoolean("sort")) {
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
                                applySorting(songs, tag, newVal)
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
    }
}

fun applySorting(songs: MutableState<List<Song>>, tag: Tag?, sortValue: Int) {
    val sorted = when(sortValue) {
        1 -> songs.value.sortedBy { it.getTagValue(tag) as? Comparable<Any> }
        2 -> songs.value.sortedByDescending { it.getTagValue(tag) as? Comparable<Any> }
        else -> songs.value
    }
    songs.value = sorted
}

fun applyFilters(songs: MutableState<List<Song>>, filterOptions: JSONArray, sorter: String) {
    var filtered = MusicLibrary.songs
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
                        TagType.STRING -> {
                            val strValue = songValue as? String ?: return@filter false
                            strValue.contains(value1.toString(), ignoreCase = true)
                        }
                        TagType.BOOLEAN -> {
                            if(value1 == 0) return@filter true
                            val boolValue = songValue as? Boolean ?: return@filter false
                            boolValue == (value1 == 1)
                        }
                        else -> {
                            val numValue = (songValue as? Number)?.toDouble() ?: return@filter false
                            val v1 = (value1 as? Number)?.toDouble() ?: Double.MIN_VALUE
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