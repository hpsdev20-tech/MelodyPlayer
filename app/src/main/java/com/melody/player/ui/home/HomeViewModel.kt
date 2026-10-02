package com.melody.player.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melody.player.data.repository.MusicRepository
import com.melody.player.model.Song
import com.melody.player.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()

    private val _recentlyAdded = MutableStateFlow<List<Song>>(emptyList())
    val recentlyAdded: StateFlow<List<Song>> = _recentlyAdded.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            repository.isLoading.collect { loading ->
                _isLoading.value = loading
            }
        }

        repository.getRecentlyPlayed()
            .onEach { _recentlyPlayed.value = it }
            .catch { /* Handle error */ }
            .launchIn(viewModelScope)

        repository.getRecentlyAdded()
            .onEach { _recentlyAdded.value = it }
            .catch { /* Handle error */ }
            .launchIn(viewModelScope)
    }

    fun playSong(song: Song, contextList: List<Song>) {
        val index = contextList.indexOf(song).takeIf { it != -1 } ?: 0
        playbackManager.play(contextList, index)
    }

    fun playAll(songs: List<Song>) {
        if (songs.isNotEmpty()) {
            playbackManager.play(songs, 0)
        }
    }
}
