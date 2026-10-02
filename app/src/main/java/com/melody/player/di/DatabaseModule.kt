package com.melody.player.di

import android.content.Context
import androidx.room.Room
import com.melody.player.data.local.MelodyDatabase
import com.melody.player.data.local.dao.AppStateDao
import com.melody.player.data.local.dao.PlaylistDao
import com.melody.player.data.local.dao.RecentlyPlayedDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMelodyDatabase(
        @ApplicationContext context: Context
    ): MelodyDatabase {
        return Room.databaseBuilder(
            context,
            MelodyDatabase::class.java,
            "melody_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun providePlaylistDao(database: MelodyDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideRecentlyPlayedDao(database: MelodyDatabase): RecentlyPlayedDao {
        return database.recentlyPlayedDao()
    }

    @Provides
    fun provideAppStateDao(database: MelodyDatabase): AppStateDao {
        return database.appStateDao()
    }
}
