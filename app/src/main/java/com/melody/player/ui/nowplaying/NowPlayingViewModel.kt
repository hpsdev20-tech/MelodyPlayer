package com.melody.player.ui.nowplaying

import androidx.lifecycle.ViewModel
import com.melody.player.model.PlaybackState
import com.melody.player.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class NowPlayingViewModel @Inject constructor(
    private val playbackManager: PlaybackManager
) : ViewModel() {
    val playbackState: StateFlow<PlaybackState> = playbackManager.playbackState

    private val _showQueue = MutableStateFlow(false)
    val showQueue: StateFlow<Boolean> = _showQueue.asStateFlow()

    fun playPause() = playbackManager.playPause()
    fun next() = playbackManager.next()
    fun previous() = playbackManager.previous()
    fun seekTo(position: Long) = playbackManager.seekTo(position)
    fun skipForward() = playbackManager.skipForward()
    fun skipBackward() = playbackManager.skipBackward()
    fun toggleShuffle() = playbackManager.toggleShuffle()
    fun cycleRepeatMode() = playbackManager.cycleRepeatMode()

    fun toggleQueue() {
        _showQueue.update { !it }
    }
    
    fun playFromQueue(index: Int) {
        val currentQueue = playbackState.value.queue
        if (index in currentQueue.indices) {
            playbackManager.play(currentQueue, index)
        }
    }
    
    fun removeFromQueue(index: Int) {
        playbackManager.removeFromQueue(index)
    }
}
