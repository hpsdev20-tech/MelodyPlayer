package com.melody.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey
    val id: Int = 0,
    val currentSongId: Long?,
    val currentPosition: Long = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = 0, // 0=OFF, 1=ONE, 2=ALL
    val queueSongIds: String = "",
    val currentIndex: Int = -1
)
