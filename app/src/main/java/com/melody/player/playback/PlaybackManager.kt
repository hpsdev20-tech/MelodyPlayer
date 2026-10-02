package com.melody.player.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.melody.player.data.local.dao.AppStateDao
import com.melody.player.data.local.entity.AppStateEntity
import com.melody.player.data.repository.MusicRepository
import com.melody.player.model.PlaybackState
import com.melody.player.model.RepeatMode
import com.melody.player.model.Song
import com.melody.player.util.toMediaItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class PlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicRepository: MusicRepository,
    private val appStateDao: AppStateDao
) {

    private var mediaController: MediaController? = null
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val songMap = mutableMapOf<String, Song>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var positionUpdateJob: Job? = null

    suspend fun connect() {
        val sessionToken = SessionToken(context, ComponentName(context, MelodyPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        try {
            mediaController = controllerFuture.await()
            setupPlayerListener()
            restoreState()
            if (mediaController?.isPlaying == true) {
                startPositionUpdater()
            }
            updateState()
        } catch (e: Exception) {
            // Handle connection failure
        }
    }

    fun disconnect() {
        scope.launch {
            saveState()
            positionUpdateJob?.cancel()
            mediaController?.release()
            mediaController = null
        }
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
                if (isPlaying) {
                    startPositionUpdater()
                } else {
                    positionUpdateJob?.cancel()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateState()
                val mediaId = mediaItem?.mediaId
                if (mediaId != null) {
                    val songId = mediaId.toLongOrNull()
                    if (songId != null) {
                        scope.launch {
                            musicRepository.recordPlayback(songId)
                        }
                    }
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateState()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updateState()
            }
        })
    }

    private fun updateState() {
        val controller = mediaController ?: return
        
        val currentMediaItem = controller.currentMediaItem
        val currentSong = currentMediaItem?.mediaId?.let { songMap[it] }
        
        val queue = mutableListOf<Song>()
        for (i in 0 until controller.mediaItemCount) {
            val item = controller.getMediaItemAt(i)
            songMap[item.mediaId]?.let { queue.add(it) }
        }

        val repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.OFF
        }

        _playbackState.update {
            it.copy(
                currentSong = currentSong,
                isPlaying = controller.isPlaying,
                position = controller.currentPosition,
                duration = controller.duration.takeIf { d -> d != C.TIME_UNSET } ?: 0L,
                shuffleEnabled = controller.shuffleModeEnabled,
                repeatMode = repeatMode,
                queue = queue,
                currentIndex = controller.currentMediaItemIndex
            )
        }
    }

    private fun startPositionUpdater() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                if (mediaController?.isPlaying == true) {
                    _playbackState.update {
                        it.copy(position = mediaController?.currentPosition ?: 0L)
                    }
                }
                delay(250)
            }
        }
    }

    fun play(songs: List<Song>, startIndex: Int = 0) {
        val controller = mediaController ?: return
        
        songMap.clear()
        songs.forEach { songMap[it.id.toString()] = it }
        
        val mediaItems = songs.map { it.toMediaItem() }
        
        controller.setMediaItems(mediaItems, startIndex, 0)
        controller.prepare()
        controller.play()
    }

    fun playPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun pause() {
        mediaController?.pause()
    }

    fun resume() {
        mediaController?.play()
    }

    fun next() {
        mediaController?.seekToNext()
    }

    fun previous() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
        } else {
            controller.seekToPrevious()
        }
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun skipForward(ms: Long = 10000) {
        val controller = mediaController ?: return
        val duration = controller.duration.takeIf { it != C.TIME_UNSET } ?: 0L
        val newPos = min(controller.currentPosition + ms, duration)
        controller.seekTo(newPos)
    }

    fun skipBackward(ms: Long = 10000) {
        val controller = mediaController ?: return
        val newPos = max(controller.currentPosition - ms, 0)
        controller.seekTo(newPos)
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    fun cycleRepeatMode() {
        val controller = mediaController ?: return
        controller.repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun addToQueue(song: Song) {
        val controller = mediaController ?: return
        songMap[song.id.toString()] = song
        controller.addMediaItem(song.toMediaItem())
        updateState()
    }

    fun removeFromQueue(index: Int) {
        val controller = mediaController ?: return
        controller.removeMediaItem(index)
        updateState()
    }

    suspend fun saveState() {
        val state = _playbackState.value
        val queueIds = state.queue.joinToString(",") { it.id.toString() }
        
        val repeatModeInt = when (state.repeatMode) {
            RepeatMode.OFF -> 0
            RepeatMode.ONE -> 1
            RepeatMode.ALL -> 2
        }

        val entity = AppStateEntity(
            currentSongId = state.currentSong?.id,
            currentPosition = state.position,
            shuffleEnabled = state.shuffleEnabled,
            repeatMode = repeatModeInt,
            queueSongIds = queueIds,
            currentIndex = state.currentIndex
        )
        appStateDao.saveState(entity)
    }

    suspend fun restoreState() {
        val state = appStateDao.getState() ?: return
        val controller = mediaController ?: return

        if (state.queueSongIds.isNotEmpty()) {
            val queueIds = state.queueSongIds.split(",").mapNotNull { it.toLongOrNull() }
            val songs = queueIds.mapNotNull { musicRepository.getSongById(it) }
            
            if (songs.isNotEmpty()) {
                val index = if (state.currentIndex in songs.indices) state.currentIndex else 0
                
                songMap.clear()
                songs.forEach { songMap[it.id.toString()] = it }
                val mediaItems = songs.map { it.toMediaItem() }
                
                controller.setMediaItems(mediaItems, index, state.currentPosition)
                controller.prepare()
                controller.pause() // Don't auto-play
                
                controller.shuffleModeEnabled = state.shuffleEnabled
                controller.repeatMode = when (state.repeatMode) {
                    1 -> Player.REPEAT_MODE_ONE
                    2 -> Player.REPEAT_MODE_ALL
                    else -> Player.REPEAT_MODE_OFF
                }
            }
        }
    }
}
