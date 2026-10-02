package com.melody.player.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melody.player.model.Song
import com.melody.player.ui.components.EmptyState
import com.melody.player.ui.components.LoadingState
import com.melody.player.ui.components.SongListItem
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import android.content.ContentUris
import android.net.Uri
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSongs: () -> Unit,
    onNavigateToAlbums: () -> Unit,
    onNavigateToArtists: () -> Unit,
    onNavigateToPlaylists: () -> Unit
) {
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") }
            )
        }
    ) { padding ->
        if (isLoading && recentlyPlayed.isEmpty() && recentlyAdded.isEmpty()) {
            LoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    QuickAccessSection(
                        onNavigateToSongs = onNavigateToSongs,
                        onNavigateToAlbums = onNavigateToAlbums,
                        onNavigateToArtists = onNavigateToArtists,
                        onNavigateToPlaylists = onNavigateToPlaylists
                    )
                }

                if (recentlyPlayed.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recently Played",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(recentlyPlayed) { song ->
                                RecentlyPlayedCard(song = song) {
                                    viewModel.playSong(song, recentlyPlayed)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Recently Added",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (recentlyAdded.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Rounded.MusicNote,
                            title = "No songs found",
                            subtitle = "Add some music to your device",
                            modifier = Modifier.fillParentMaxHeight(0.5f)
                        )
                    }
                } else {
                    items(recentlyAdded) { song ->
                        SongListItem(
                            song = song,
                            onClick = { viewModel.playSong(song, recentlyAdded) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAccessSection(
    onNavigateToSongs: () -> Unit,
    onNavigateToAlbums: () -> Unit,
    onNavigateToArtists: () -> Unit,
    onNavigateToPlaylists: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickAccessItem("Songs", Icons.Rounded.MusicNote, onNavigateToSongs)
        QuickAccessItem("Albums", Icons.Rounded.Album, onNavigateToAlbums)
        QuickAccessItem("Artists", Icons.Rounded.Person, onNavigateToArtists)
        QuickAccessItem("Playlists", Icons.Rounded.PlaylistPlay, onNavigateToPlaylists)
    }
}

@Composable
fun QuickAccessItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, modifier = Modifier.size(72.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun RecentlyPlayedCard(song: Song, onClick: () -> Unit) {
    val albumArtUri = ContentUris.withAppendedId(
        Uri.parse("content://media/external/audio/albumart"),
        song.albumId
    )
    ElevatedCard(onClick = onClick, modifier = Modifier.width(140.dp)) {
        Column {
            AsyncImage(
                model = albumArtUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
