package com.melody.player.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.melody.player.data.repository.MusicRepository
import com.melody.player.playback.PlaybackManager
import com.melody.player.ui.albums.AlbumDetailScreen
import com.melody.player.ui.albums.AlbumsScreen
import com.melody.player.ui.artists.ArtistDetailScreen
import com.melody.player.ui.artists.ArtistsScreen
import com.melody.player.ui.components.MiniPlayer
import com.melody.player.ui.home.HomeScreen
import com.melody.player.ui.nowplaying.NowPlayingScreen
import com.melody.player.ui.playlists.PlaylistDetailScreen
import com.melody.player.ui.playlists.PlaylistsScreen
import com.melody.player.ui.songs.SongsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Songs : Screen("songs")
    object Albums : Screen("albums")
    object AlbumDetail : Screen("album/{albumId}/{albumName}") {
        fun createRoute(albumId: Long, albumName: String): String {
            val encodedName = java.net.URLEncoder.encode(albumName, "UTF-8")
            return "album/$albumId/$encodedName"
        }
    }
    object Artists : Screen("artists")
    object ArtistDetail : Screen("artist/{artistId}/{artistName}") {
        fun createRoute(artistId: Long, artistName: String): String {
            val encodedName = java.net.URLEncoder.encode(artistName, "UTF-8")
            return "artist/$artistId/$encodedName"
        }
    }
    object Playlists : Screen("playlists")
    object PlaylistDetail : Screen("playlist/{playlistId}/{playlistName}") {
        fun createRoute(playlistId: Long, playlistName: String): String {
            val encodedName = java.net.URLEncoder.encode(playlistName, "UTF-8")
            return "playlist/$playlistId/$encodedName"
        }
    }
    object NowPlaying : Screen("now_playing")
}

@Composable
fun MelodyAppComposable(
    playbackManager: PlaybackManager,
    musicRepository: MusicRepository
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            CoroutineScope(Dispatchers.IO).launch {
                musicRepository.scanMedia()
            }
        }
    }

    LaunchedEffect(Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions)
    }

    val navController = rememberNavController()
    val playbackState by playbackManager.playbackState.collectAsStateWithLifecycle()

    val bottomNavItems = listOf(
        Triple(Screen.Home, Icons.Rounded.Home, "Home"),
        Triple(Screen.Songs, Icons.Rounded.MusicNote, "Songs"),
        Triple(Screen.Albums, Icons.Rounded.Album, "Albums"),
        Triple(Screen.Artists, Icons.Rounded.Person, "Artists"),
        Triple(Screen.Playlists, Icons.Rounded.PlaylistPlay, "Playlists")
    )

    Scaffold(
        bottomBar = {
            Column {
                MiniPlayer(
                    playbackState = playbackState,
                    onPlayPause = { playbackManager.playPause() },
                    onNext = { playbackManager.next() },
                    onClick = {
                        navController.navigate(Screen.NowPlaying.route) {
                            launchSingleTop = true
                        }
                    }
                )

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomNavVisible = currentDestination?.route in bottomNavItems.map { it.first.route }

                if (bottomNavVisible) {
                    NavigationBar {
                        bottomNavItems.forEach { (screen, icon, label) ->
                            NavigationBarItem(
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToSongs = { navController.navigate(Screen.Songs.route) },
                    onNavigateToAlbums = { navController.navigate(Screen.Albums.route) },
                    onNavigateToArtists = { navController.navigate(Screen.Artists.route) },
                    onNavigateToPlaylists = { navController.navigate(Screen.Playlists.route) }
                )
            }

            composable(Screen.Songs.route) {
                SongsScreen()
            }

            composable(Screen.Albums.route) {
                AlbumsScreen(
                    onAlbumClick = { album ->
                        navController.navigate(Screen.AlbumDetail.createRoute(album.id, album.name))
                    }
                )
            }

            composable(
                route = Screen.AlbumDetail.route,
                arguments = listOf(
                    navArgument("albumId") { type = NavType.LongType },
                    navArgument("albumName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val albumId = backStackEntry.arguments?.getLong("albumId") ?: 0L
                val albumName = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("albumName") ?: "",
                    "UTF-8"
                )
                AlbumDetailScreen(
                    albumId = albumId,
                    albumName = albumName,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Artists.route) {
                ArtistsScreen(
                    onArtistClick = { artist ->
                        navController.navigate(Screen.ArtistDetail.createRoute(artist.id, artist.name))
                    }
                )
            }

            composable(
                route = Screen.ArtistDetail.route,
                arguments = listOf(
                    navArgument("artistId") { type = NavType.LongType },
                    navArgument("artistName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val artistId = backStackEntry.arguments?.getLong("artistId") ?: 0L
                val artistName = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("artistName") ?: "",
                    "UTF-8"
                )
                ArtistDetailScreen(
                    artistId = artistId,
                    artistName = artistName,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Playlists.route) {
                PlaylistsScreen(
                    onPlaylistClick = { playlist ->
                        navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id, playlist.name))
                    }
                )
            }

            composable(
                route = Screen.PlaylistDetail.route,
                arguments = listOf(
                    navArgument("playlistId") { type = NavType.LongType },
                    navArgument("playlistName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                val playlistName = java.net.URLDecoder.decode(
                    backStackEntry.arguments?.getString("playlistName") ?: "",
                    "UTF-8"
                )
                PlaylistDetailScreen(
                    playlistId = playlistId,
                    playlistName = playlistName,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NowPlaying.route) {
                NowPlayingScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
