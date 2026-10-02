package com.melody.player.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.melody.player.data.local.entity.RecentlyPlayedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyPlayedDao {
    @Insert
    suspend fun insert(entity: RecentlyPlayedEntity)

    @Query("SELECT * FROM recently_played WHERE playedAt IN (SELECT MAX(playedAt) FROM recently_played GROUP BY songId) ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<RecentlyPlayedEntity>>

    @Query("DELETE FROM recently_played WHERE id NOT IN (SELECT id FROM recently_played ORDER BY playedAt DESC LIMIT :keepCount)")
    suspend fun deleteOldEntries(keepCount: Int = 100)
}
