package com.mvukosav.scoreagentsvas.user.di

import android.content.Context
import com.mvukosav.scoreagentsvas.service.ScoreServices
import com.mvukosav.scoreagentsvas.user.data.repository.UserRepositoryImpl
import com.mvukosav.scoreagentsvas.user.domain.repository.UserRepository
import com.mvukosav.scoreagentsvas.user.domain.usecase.GetUser
import com.mvukosav.scoreagentsvas.user.domain.usecase.IsUserLoggedIn
import com.mvukosav.scoreagentsvas.user.domain.usecase.LoginUser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideUserRepository(@ApplicationContext context: Context): UserRepository =
        UserRepositoryImpl(context = context)

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // You can change the level as needed
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()


        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideScoreServices(retrofit: Retrofit): ScoreServices {
        return retrofit.create(ScoreServices::class.java)
    }


    //useCases
    @Provides
    @Singleton
    fun provideIsUserLoggedIn(userRepository: UserRepository): IsUserLoggedIn =
        IsUserLoggedIn(userRepository)

    @Provides
    @Singleton
    fun provideGetUser(userRepository: UserRepository): GetUser =
        GetUser(userRepository)

    @Provides
    @Singleton
    fun provideLoginUser(userRepository: UserRepository): LoginUser =
        LoginUser(userRepository)

}

private const val BASE_URL = "https://api.soccerdataapi.com/"
private const val AUTH_TOKEN = "9b36a11c0cca1a860b533a5398474823d03c6f0e"

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalHttpUrl = originalRequest.url

        val newHttpUrl = originalHttpUrl.newBuilder()
            .addQueryParameter("auth_token", AUTH_TOKEN)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newHttpUrl)
            .build()

        return chain.proceed(newRequest)
    }
}