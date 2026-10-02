package com.melody.player.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.melody.player.data.local.dao.AppStateDao
import com.melody.player.data.local.dao.PlaylistDao
import com.melody.player.data.local.dao.RecentlyPlayedDao
import com.melody.player.data.local.entity.AppStateEntity
import com.melody.player.data.local.entity.PlaylistEntity
import com.melody.player.data.local.entity.PlaylistSongCrossRef
import com.melody.player.data.local.entity.RecentlyPlayedEntity

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        RecentlyPlayedEntity::class,
        AppStateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MelodyDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun appStateDao(): AppStateDao
}
