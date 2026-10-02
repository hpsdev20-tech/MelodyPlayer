package com.melody.player.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // MediaStoreScanner uses @Singleton @Inject constructor so Hilt handles it automatically.
    // Additional application-scoped providers can be added here.
}
