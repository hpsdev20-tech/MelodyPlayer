package com.melody.player.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.melody.player.data.local.entity.AppStateEntity

@Dao
interface AppStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveState(state: AppStateEntity)

    @Query("SELECT * FROM app_state WHERE id = 0")
    suspend fun getState(): AppStateEntity?
}
