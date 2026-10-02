package com.melody.player.ui.songs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melody.player.data.repository.MusicRepository
import com.melody.player.model.Song
import com.melody.player.model.SortOrder
import com.melody.player.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongsViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.TITLE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    val isLoading: StateFlow<Boolean> = repository.isLoading

    val songs: StateFlow<List<Song>> = combine(
        repository.songs,
        _searchQuery,
        _sortOrder
    ) { allSongs, query, sort ->
        var filtered = allSongs
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
        }
        
        when (sort) {
            SortOrder.TITLE -> filtered.sortedBy { it.title }
            SortOrder.ARTIST -> filtered.sortedBy { it.artist }
            SortOrder.ALBUM -> filtered.sortedBy { it.album }
            SortOrder.DATE_ADDED -> filtered.sortedByDescending { it.dateAdded }
            SortOrder.DURATION -> filtered.sortedByDescending { it.duration }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playbackState = playbackManager.playbackState

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun updateSort(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    fun playSong(song: Song, index: Int) {
        playbackManager.play(songs.value, index)
    }

    fun playAll() {
        if (songs.value.isNotEmpty()) {
            playbackManager.play(songs.value, 0)
        }
    }
}
