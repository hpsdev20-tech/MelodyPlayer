package com.melody.player.ui.artists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melody.player.data.repository.MusicRepository
import com.melody.player.model.Song
import com.melody.player.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val musicRepository: MusicRepository,
    private val playbackManager: PlaybackManager
) : ViewModel() {
    private val artistName: String = savedStateHandle.get<String>("artistName") ?: ""

    val songs: StateFlow<List<Song>> = musicRepository.getSongsByArtist(artistName)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isLoading: StateFlow<Boolean> = musicRepository.isLoading
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

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
