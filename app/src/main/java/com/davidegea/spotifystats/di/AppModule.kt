package com.davidegea.spotifystats.di

import android.content.Context
import androidx.room.Room
import com.davidegea.spotifystats.data.history.RoomListeningHistoryRepository
import com.davidegea.spotifystats.data.importer.RoomSpotifyHistoryImportRepository
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.database.dao.ImportDao
import com.davidegea.spotifystats.database.dao.ListeningHistoryDao
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import com.davidegea.spotifystats.domain.repository.SpotifyHistoryImportRepository
import com.davidegea.spotifystats.domain.usecase.ImportSpotifyHistoryUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveAlbumDetailUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveArtistDetailUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveHomeDashboardUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveLibraryRankingsUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveListeningHistoryUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveOverviewStatsUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveTrackDetailUseCase
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
    fun provideImportDao(
        database: SpotifyStatsDatabase,
    ): ImportDao = database.importDao()

    @Provides
    @Singleton
    fun provideListeningHistoryRepository(
        dao: ListeningHistoryDao,
    ): ListeningHistoryRepository = RoomListeningHistoryRepository(dao)

    @Provides
    @Singleton
    fun provideSpotifyHistoryImportRepository(
        @ApplicationContext context: Context,
        database: SpotifyStatsDatabase,
        importDao: ImportDao,
    ): SpotifyHistoryImportRepository = RoomSpotifyHistoryImportRepository(
        context = context,
        database = database,
        importDao = importDao,
    )

    @Provides
    fun provideObserveOverviewStatsUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveOverviewStatsUseCase = ObserveOverviewStatsUseCase(repository)

    @Provides
    fun provideObserveHomeDashboardUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveHomeDashboardUseCase = ObserveHomeDashboardUseCase(repository)

    @Provides
    fun provideObserveLibraryRankingsUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveLibraryRankingsUseCase = ObserveLibraryRankingsUseCase(repository)

    @Provides
    fun provideObserveTrackDetailUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveTrackDetailUseCase = ObserveTrackDetailUseCase(repository)

    @Provides
    fun provideObserveArtistDetailUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveArtistDetailUseCase = ObserveArtistDetailUseCase(repository)

    @Provides
    fun provideObserveAlbumDetailUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveAlbumDetailUseCase = ObserveAlbumDetailUseCase(repository)

    @Provides
    fun provideObserveListeningHistoryUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveListeningHistoryUseCase = ObserveListeningHistoryUseCase(repository)

    @Provides
    fun provideImportSpotifyHistoryUseCase(
        repository: SpotifyHistoryImportRepository,
    ): ImportSpotifyHistoryUseCase = ImportSpotifyHistoryUseCase(repository)
}
