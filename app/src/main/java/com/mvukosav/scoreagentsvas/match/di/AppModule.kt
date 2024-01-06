package com.mvukosav.scoreagentsvas.match.di

import android.content.Context
import com.mvukosav.scoreagentsvas.match.data.repository.DataToDomainLiveScores
import com.mvukosav.scoreagentsvas.match.data.repository.DataToDomainMatchDetails
import com.mvukosav.scoreagentsvas.match.data.repository.MatchesRepositoryImpl
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import com.mvukosav.scoreagentsvas.match.domain.usecase.RefreshMatches
import com.mvukosav.scoreagentsvas.service.AgentsNotificationService
import com.mvukosav.scoreagentsvas.service.ScoreServices
import com.mvukosav.scoreagentsvas.utils.AgentsNotificationServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Provides
    @Singleton
    fun provideMatchesRepository(
        apiServices: ScoreServices,
        @ApplicationContext context: Context,
        agentsNotificationService: AgentsNotificationService,
        dataToDomainLiveScores: DataToDomainLiveScores,
        dataToDomainMatchDetails: DataToDomainMatchDetails
    ): MatchesRepository =
        MatchesRepositoryImpl(
            apiServices,
            context = context,
            agentsNotificationService,
            dataToDomainLiveScores,
            dataToDomainMatchDetails
        )

    @Provides
    @Singleton
    fun provideDataToDomainLivescore(): DataToDomainLiveScores = DataToDomainLiveScores()

    @Provides
    @Singleton
    fun provideDataToDomainMatchDetails(): DataToDomainMatchDetails = DataToDomainMatchDetails()

    @Provides
    @Singleton
    fun provideGetMatches(matchesRepository: MatchesRepository): RefreshMatches =
        RefreshMatches(matchesRepository)

    @Provides
    @Singleton
    fun provideAgentsNotification(@ApplicationContext context: Context): AgentsNotificationService =
        AgentsNotificationServiceImpl(context)
}