package com.mvukosav.scoreagentsvas.match.di

import android.content.Context
import com.mvukosav.scoreagentsvas.match.data.repository.MatchesRepositoryImpl
import com.mvukosav.scoreagentsvas.match.domain.repository.MatchesRepository
import com.mvukosav.scoreagentsvas.match.domain.usecase.GetMatches
import com.mvukosav.scoreagentsvas.service.ScoreServices
import com.mvukosav.scoreagentsvas.user.domain.repository.UserRepository
import com.mvukosav.scoreagentsvas.user.domain.usecase.LoginUser
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
    fun provideMatchesRepository(apiServices: ScoreServices, @ApplicationContext context: Context): MatchesRepository =
        MatchesRepositoryImpl(apiServices, context = context)

    @Provides
    @Singleton
    fun provideGetMatches(matchesRepository: MatchesRepository): GetMatches =
        GetMatches(matchesRepository)

}