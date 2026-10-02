package com.melody.player.ui.albums

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melody.player.data.repository.MusicRepository
import com.melody.player.model.Song
import com.melody.player.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {
    private val albumId: Long = checkNotNull(savedStateHandle.get<Long>("albumId"))
    val albumName: String = savedStateHandle.get<String>("albumName") ?: "Unknown Album"

    val songs: StateFlow<List<Song>> = musicRepository.getSongsByAlbum(albumId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isLoading: StateFlow<Boolean> = musicRepository.isLoading
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val albumArtUri: StateFlow<Uri?> = musicRepository.albums
        .map { albums -> albums.find { it.id == albumId }?.artUri }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
}
