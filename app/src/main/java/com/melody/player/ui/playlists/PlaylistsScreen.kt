package com.melody.player.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.melody.player.model.Playlist
import com.melody.player.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
    onPlaylistClick: (Playlist) -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var renamePlaylist by remember { mutableStateOf<Playlist?>(null) }

    if (showCreateDialog) {
        PlaylistDialog(
            title = "Create Playlist",
            initialName = "",
            onConfirm = { name ->
                viewModel.createPlaylist(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    renamePlaylist?.let { playlist ->
        PlaylistDialog(
            title = "Rename Playlist",
            initialName = playlist.name,
            onConfirm = { name ->
                viewModel.renamePlaylist(playlist.id, name)
                renamePlaylist = null
            },
            onDismiss = { renamePlaylist = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Playlists") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Create Playlist")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (playlists.isEmpty()) {
                EmptyState(
                    icon = Icons.Rounded.QueueMusic,
                    title = "No Playlists",
                    subtitle = "Create your first playlist by tapping the + button"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        var expanded by remember { mutableStateOf(false) }
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            supportingContent = { Text("${playlist.songCount} songs") },
                            leadingContent = {
                                Icon(Icons.Rounded.QueueMusic, contentDescription = null)
                            },
                            trailingContent = {
                                Box {
                                    IconButton(onClick = { expanded = true }) {
                                        Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Rename") },
                                            onClick = {
                                                expanded = false
                                                renamePlaylist = playlist
                                            },
                                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Delete") },
                                            onClick = {
                                                expanded = false
                                                viewModel.deletePlaylist(playlist.id)
                                            },
                                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.clickable { onPlaylistClick(playlist) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaylistDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Playlist Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
