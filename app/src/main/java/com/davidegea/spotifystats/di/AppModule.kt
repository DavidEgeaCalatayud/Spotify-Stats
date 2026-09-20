package com.davidegea.spotifystats.di

import android.content.Context
import androidx.room.Room
import com.davidegea.spotifystats.data.history.RoomListeningHistoryRepository
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.database.dao.ListeningHistoryDao
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): SpotifyStatsDatabase = Room.databaseBuilder(
        context,
        SpotifyStatsDatabase::class.java,
        SpotifyStatsDatabase.DATABASE_NAME,
    ).build()

    @Provides
    fun provideListeningHistoryDao(
        database: SpotifyStatsDatabase,
    ): ListeningHistoryDao = database.listeningHistoryDao()

    @Provides
    @Singleton
    fun provideListeningHistoryRepository(
        dao: ListeningHistoryDao,
    ): ListeningHistoryRepository = RoomListeningHistoryRepository(dao)
}
