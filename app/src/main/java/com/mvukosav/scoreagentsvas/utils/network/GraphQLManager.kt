package com.mvukosav.scoreagentsvas.utils.network

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.mvukosav.scoreagentsvas.user.di.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object GraphQlManager {

    private var instance: ApolloClient? = null

    fun apolloClient(): ApolloClient {
        if (instance != null) {
            return instance!!
        }

        instance = ApolloClient.Builder()
            .serverUrl(PRODUCTION)
            .okHttpClient(returnOkHttpClient())
            .build()

        return instance!!
    }

    private fun returnOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // You can change the level as needed
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }
}

const val LOCAL_HOST = "http://10.0.2.2:8000/graphql"

const val PRODUCTION = "https://vas-b-marko2407.koyeb.app/graphql"