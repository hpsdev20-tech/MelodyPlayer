package com.melody.player.data.repository

import com.melody.player.data.local.dao.RecentlyPlayedDao
import com.melody.player.data.local.entity.RecentlyPlayedEntity
import com.melody.player.data.media.MediaStoreScanner
import com.melody.player.model.Album
import com.melody.player.model.Artist
import com.melody.player.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val scanner: MediaStoreScanner,
    private val recentlyPlayedDao: RecentlyPlayedDao
) : MusicRepository {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    override val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    override val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    override val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    override suspend fun scanMedia() = withContext(Dispatchers.IO) {
        _isLoading.value = true
        try {
            val scannedSongs = scanner.scanSongs().filter { File(it.path).exists() }
            _songs.value = scannedSongs
            _albums.value = scanner.scanAlbums()
            _artists.value = scanner.scanArtists()
        } finally {
            _isLoading.value = false
        }
    }

    override fun getSongsByAlbum(albumId: Long): Flow<List<Song>> {
        return _songs.map { allSongs ->
            allSongs.filter { it.albumId == albumId }
        }
    }

    override fun getSongsByArtist(artistName: String): Flow<List<Song>> {
        return _songs.map { allSongs ->
            allSongs.filter { it.artist == artistName }
        }
    }

    override fun getRecentlyPlayed(): Flow<List<Song>> {
        return recentlyPlayedDao.getRecentlyPlayed().map { recentEntities ->
            val songMap = _songs.value.associateBy { it.id }
            recentEntities.mapNotNull { entity ->
                songMap[entity.songId]
            }
        }
    }

    override fun getRecentlyAdded(limit: Int): Flow<List<Song>> {
        return _songs.map { allSongs ->
            allSongs.sortedByDescending { it.dateAdded }.take(limit)
        }
    }

    override fun searchSongs(query: String): Flow<List<Song>> {
        return _songs.map { allSongs ->
            if (query.isBlank()) emptyList()
            else allSongs.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.artist.contains(query, ignoreCase = true) ||
                it.album.contains(query, ignoreCase = true)
            }
        }
    }

    override suspend fun recordPlayback(songId: Long) {
        recentlyPlayedDao.insert(
            RecentlyPlayedEntity(
                songId = songId,
                playedAt = System.currentTimeMillis()
            )
        )
        recentlyPlayedDao.deleteOldEntries()
    }

    override fun getSongById(songId: Long): Song? {
        return _songs.value.find { it.id == songId }
    }
}
