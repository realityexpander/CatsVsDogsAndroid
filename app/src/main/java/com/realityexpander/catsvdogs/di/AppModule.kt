package com.realityexpander.catsvdogs.di

import com.realityexpander.catsvdogs.data.KtorRealtimeMessagingClientImpl
import com.realityexpander.catsvdogs.data.IRealtimeMessagingClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient {
        return HttpClient(CIO) {
            install(Logging)
            install(WebSockets)
        }
    }

    @Singleton
    @Provides
    fun provideRealtimeMessagingClient(httpClient: HttpClient): IRealtimeMessagingClient {
        return KtorRealtimeMessagingClientImpl(httpClient)
    }
}