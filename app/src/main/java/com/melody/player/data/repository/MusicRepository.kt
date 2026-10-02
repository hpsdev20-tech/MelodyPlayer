package com.melody.player.data.repository

import com.melody.player.model.Album
import com.melody.player.model.Artist
import com.melody.player.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MusicRepository {
    val songs: StateFlow<List<Song>>
    val albums: StateFlow<List<Album>>
    val artists: StateFlow<List<Artist>>
    val isLoading: StateFlow<Boolean>
    
    fun getSongsByAlbum(albumId: Long): Flow<List<Song>>
    fun getSongsByArtist(artistName: String): Flow<List<Song>>
    fun getRecentlyPlayed(): Flow<List<Song>>
    fun getRecentlyAdded(limit: Int = 20): Flow<List<Song>>
    fun searchSongs(query: String): Flow<List<Song>>
    suspend fun scanMedia()
    suspend fun recordPlayback(songId: Long)
    fun getSongById(songId: Long): Song?
}
