package com.melody.player.ui.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melody.player.data.repository.PlaylistRepository
import com.melody.player.model.Song
import com.melody.player.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {
    private val playlistId: Long = checkNotNull(savedStateHandle.get<Long>("playlistId"))
    val playlistName: String = savedStateHandle.get<String>("playlistName") ?: "Unknown Playlist"

    val songs: StateFlow<List<Song>> = playlistRepository.getPlaylistSongs(playlistId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // A simple loading state since PlaylistRepository might not expose one directly for this flow
    val isLoading: StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(false)

    fun playSong(index: Int) {
        val currentSongs = songs.value
        if (currentSongs.isNotEmpty() && index in currentSongs.indices) {
            playbackManager.play(currentSongs, index)
        }
    }

    fun playAll() {
        val currentSongs = songs.value
        if (currentSongs.isNotEmpty()) {
            if (playbackManager.playbackState.value.shuffleEnabled) {
                playbackManager.toggleShuffle()
            }
            playbackManager.play(currentSongs, 0)
        }
    }

    fun shuffleAll() {
        val currentSongs = songs.value
        if (currentSongs.isNotEmpty()) {
            if (!playbackManager.playbackState.value.shuffleEnabled) {
                playbackManager.toggleShuffle()
            }
            playbackManager.play(currentSongs, 0)
        }
    }

    fun removeSong(songId: Long) {
        viewModelScope.launch {
            playlistRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }
}
