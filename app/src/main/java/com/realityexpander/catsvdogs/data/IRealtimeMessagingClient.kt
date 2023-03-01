package com.realityexpander.catsvdogs.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

interface IRealtimeMessagingClient {
    fun getGameStateFlow(): Flow<GameState>
    suspend fun sendAction(action: MakeTurn)
    suspend fun close()
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun handlePings(onSuccess: () -> Unit = {}, onError: () -> Unit = {})
}