package com.davidegea.spotifystats.di

import android.content.Context
import androidx.room.Room
import com.davidegea.spotifystats.data.history.RoomListeningHistoryRepository
import com.davidegea.spotifystats.data.importer.RoomSpotifyHistoryImportRepository
import com.davidegea.spotifystats.database.SpotifyStatsDatabase
import com.davidegea.spotifystats.database.dao.ImportDao
import com.davidegea.spotifystats.database.dao.ImportJobDao
import com.davidegea.spotifystats.database.dao.ListeningHistoryDao
import com.davidegea.spotifystats.domain.repository.ListeningHistoryRepository
import com.davidegea.spotifystats.domain.repository.SpotifyHistoryImportRepository
import com.davidegea.spotifystats.domain.repository.SpotifyImportJobManager
import com.davidegea.spotifystats.data.importer.WorkManagerSpotifyImportJobManager
import com.davidegea.spotifystats.domain.usecase.ImportSpotifyHistoryUseCase
import com.davidegea.spotifystats.domain.usecase.LoadListeningHistoryPageUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveAlbumDetailUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveArtistDetailUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveArtistRangeRankUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveHomeDashboardUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveLibraryRankingsUseCase
import com.davidegea.spotifystats.domain.usecase.ObserveListeningHabitsUseCase
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
    ).addMigrations(
        com.davidegea.spotifystats.database.MIGRATION_1_2,
        com.davidegea.spotifystats.database.MIGRATION_2_3,
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
    fun provideImportJobDao(
        database: SpotifyStatsDatabase,
    ): ImportJobDao = database.importJobDao()

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
    @Singleton
    fun provideExplorationRepository(database: SpotifyStatsDatabase, history: ListeningHistoryRepository): com.davidegea.spotifystats.domain.repository.ExplorationRepository =
        com.davidegea.spotifystats.data.history.RoomExplorationRepository(database, history)

    @Provides
    @Singleton
    fun provideDataControlRepository(@ApplicationContext context: Context, database: SpotifyStatsDatabase): com.davidegea.spotifystats.domain.repository.DataControlRepository =
        com.davidegea.spotifystats.data.privacy.LocalDataControlRepository(context, database)

    @Provides
    fun provideManageLocalDataUseCase(repository: com.davidegea.spotifystats.domain.repository.DataControlRepository) =
        com.davidegea.spotifystats.domain.usecase.ManageLocalDataUseCase(repository)

    @Provides
    fun provideExploreListeningUseCase(repository: com.davidegea.spotifystats.domain.repository.ExplorationRepository) =
        com.davidegea.spotifystats.domain.usecase.ExploreListeningUseCase(repository)

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
    fun provideObserveArtistRangeRankUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveArtistRangeRankUseCase = ObserveArtistRangeRankUseCase(repository)

    @Provides
    fun provideObserveAlbumDetailUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveAlbumDetailUseCase = ObserveAlbumDetailUseCase(repository)

    @Provides
    fun provideObserveListeningHistoryUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveListeningHistoryUseCase = ObserveListeningHistoryUseCase(repository)

    @Provides
    fun provideLoadListeningHistoryPageUseCase(
        repository: ListeningHistoryRepository,
    ): LoadListeningHistoryPageUseCase = LoadListeningHistoryPageUseCase(repository)

    @Provides
    fun provideObserveListeningHabitsUseCase(
        repository: ListeningHistoryRepository,
    ): ObserveListeningHabitsUseCase = ObserveListeningHabitsUseCase(repository)

    @Provides
    @Singleton
    fun provideSpotifyImportJobManager(
        @ApplicationContext context: Context,
        database: SpotifyStatsDatabase,
        importJobDao: ImportJobDao,
    ): SpotifyImportJobManager = WorkManagerSpotifyImportJobManager(
        context = context,
        database = database,
        importJobDao = importJobDao,
    )

    @Provides
    fun provideImportSpotifyHistoryUseCase(
        repository: SpotifyHistoryImportRepository,
    ): ImportSpotifyHistoryUseCase = ImportSpotifyHistoryUseCase(repository)
}
