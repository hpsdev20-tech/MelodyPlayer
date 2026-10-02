package com.melody.player.data.repository

import com.melody.player.data.local.dao.PlaylistDao
import com.melody.player.data.local.entity.PlaylistEntity
import com.melody.player.data.local.entity.PlaylistSongCrossRef
import com.melody.player.model.Playlist
import com.melody.player.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val musicRepository: MusicRepository
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { entity ->
                val count = playlistDao.getPlaylistSongCount(entity.id)
                Playlist(
                    id = entity.id,
                    name = entity.name,
                    songCount = count,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    override fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getPlaylistSongIds(playlistId).map { songIds ->
            val allSongs = musicRepository.songs.value.associateBy { it.id }
            songIds.mapNotNull { id -> allSongs[id] }
        }
    }

    override suspend fun createPlaylist(name: String): Long {
        val now = System.currentTimeMillis()
        val entity = PlaylistEntity(
            name = name,
            createdAt = now,
            updatedAt = now
        )
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun renamePlaylist(id: Long, name: String) {
        val playlist = playlistDao.getPlaylistById(id)
        if (playlist != null) {
            playlistDao.updatePlaylist(
                playlist.copy(
                    name = name,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun deletePlaylist(id: Long) {
        playlistDao.deletePlaylist(id)
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        val currentMaxIndex = playlistDao.getMaxOrderIndex(playlistId) ?: -1
        val crossRef = PlaylistSongCrossRef(
            playlistId = playlistId,
            songId = songId,
            addedAt = System.currentTimeMillis(),
            orderIndex = currentMaxIndex + 1
        )
        playlistDao.addSongToPlaylist(crossRef)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }
}
