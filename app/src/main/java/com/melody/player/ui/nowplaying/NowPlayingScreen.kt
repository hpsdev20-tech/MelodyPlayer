package com.melody.player.ui.nowplaying

import android.content.ContentUris
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.melody.player.model.PlaybackState
import com.melody.player.model.RepeatMode
import com.melody.player.ui.components.SongListItem
import com.melody.player.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onBack: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val showQueue by viewModel.showQueue.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ExpandMore, contentDescription = "Collapse")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleQueue() }) {
                        Icon(
                            Icons.Rounded.QueueMusic,
                            contentDescription = "Queue",
                            tint = if (showQueue) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = showQueue,
                modifier = Modifier.weight(1f)
            ) { isQueueVisible ->
                if (isQueueVisible) {
                    QueueView(
                        playbackState = playbackState,
                        onPlaySong = { viewModel.playFromQueue(it) },
                        onRemoveSong = { viewModel.removeFromQueue(it) }
                    )
                } else {
                    NowPlayingDetails(
                        playbackState = playbackState,
                        onSeek = { viewModel.seekTo(it) },
                        onPlayPause = { viewModel.playPause() },
                        onNext = { viewModel.next() },
                        onPrevious = { viewModel.previous() },
                        onSkipForward = { viewModel.skipForward() },
                        onSkipBackward = { viewModel.skipBackward() },
                        onToggleShuffle = { viewModel.toggleShuffle() },
                        onCycleRepeat = { viewModel.cycleRepeatMode() }
                    )
                }
            }
        }
    }
}

@Composable
fun NowPlayingDetails(
    playbackState: PlaybackState,
    onSeek: (Long) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit
) {
    val currentSong = playbackState.currentSong
    val albumArtUri = currentSong?.let {
        ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), it.albumId)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Album Art
        ElevatedCard(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            if (albumArtUri != null) {
                AsyncImage(
                    model = albumArtUri,
                    contentDescription = currentSong.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title and Artist
        Text(
            text = currentSong?.title ?: "Not Playing",
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = currentSong?.artist ?: "Unknown Artist",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Progress Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = if (playbackState.duration > 0) playbackState.position.toFloat() / playbackState.duration else 0f,
                onValueChange = { value ->
                    onSeek((value * playbackState.duration).toLong())
                },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = TimeUtils.formatDuration(playbackState.position),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = TimeUtils.formatDuration(playbackState.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (playbackState.shuffleEnabled) MaterialTheme.colorScheme.primary else LocalContentColor.current
                )
            }
            IconButton(onClick = onPrevious) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp))
            }
            FilledIconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    if (playbackState.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = onCycleRepeat) {
                val icon = when (playbackState.repeatMode) {
                    RepeatMode.OFF -> Icons.Rounded.Repeat
                    RepeatMode.ONE -> Icons.Rounded.Repeat // Usually requires a special icon, using Repeat for now
                    RepeatMode.ALL -> Icons.Rounded.Repeat
                }
                val tint = if (playbackState.repeatMode != RepeatMode.OFF) MaterialTheme.colorScheme.primary else LocalContentColor.current
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, contentDescription = "Repeat", tint = tint)
                    if (playbackState.repeatMode == RepeatMode.ONE) {
                        Text("1", style = MaterialTheme.typography.labelSmall, color = tint)
                    } else if (playbackState.repeatMode == RepeatMode.ALL) {
                        Text("All", style = MaterialTheme.typography.labelSmall, color = tint)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Skip controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSkipBackward) {
                Icon(Icons.Rounded.Replay10, contentDescription = "Backward 10s")
                Spacer(Modifier.width(4.dp))
                Text("-10s")
            }
            Spacer(Modifier.width(32.dp))
            TextButton(onClick = onSkipForward) {
                Text("+10s")
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Rounded.Forward10, contentDescription = "Forward 10s")
            }
        }
    }
}

@Composable
fun QueueView(
    playbackState: PlaybackState,
    onPlaySong: (Int) -> Unit,
    onRemoveSong: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Up Next",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            itemsIndexed(playbackState.queue, key = { index, song -> "${song.id}_$index" }) { index, song ->
                val isPlaying = index == playbackState.currentIndex
                SongListItem(
                    song = song,
                    onClick = { onPlaySong(index) },
                    isPlaying = isPlaying,
                    onMoreClick = { onRemoveSong(index) }
                )
            }
        }
    }
}
