package com.melody.player.ui.artists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.melody.player.data.repository.MusicRepository
import com.melody.player.model.Artist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistsViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {
    val artists: StateFlow<List<Artist>> = musicRepository.artists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isLoading: StateFlow<Boolean> = musicRepository.isLoading
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
}
