package com.melody.player.ui.songs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melody.player.ui.components.EmptyState
import com.melody.player.ui.components.LoadingState
import com.melody.player.ui.components.MelodySearchBar
import com.melody.player.ui.components.SongListItem
import com.melody.player.ui.components.SortMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongsScreen(
    viewModel: SongsViewModel = hiltViewModel()
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Songs") },
                actions = {
                    SortMenu(
                        currentSort = sortOrder,
                        onSortChanged = { viewModel.updateSort(it) }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MelodySearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearch(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${songs.size} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (songs.isNotEmpty()) {
                    FilledTonalButton(onClick = { viewModel.playAll() }) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Play All")
                    }
                }
            }

            if (isLoading && songs.isEmpty()) {
                LoadingState()
            } else if (songs.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.MusicNote,
                    title = "No songs found",
                    subtitle = if (searchQuery.isNotEmpty()) "Try a different search" else "Add some music to your device"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                        val isPlaying = playbackState.currentSong?.id == song.id
                        SongListItem(
                            song = song,
                            onClick = { viewModel.playSong(song, index) },
                            isPlaying = isPlaying,
                            onMoreClick = { /* Handle more actions */ }
                        )
                    }
                }
            }
        }
    }
}
