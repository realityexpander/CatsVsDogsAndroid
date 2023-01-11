package com.realityexpander.catsvdogs.data

import com.realityexpander.catsvdogs.presentation.Player
import com.realityexpander.catsvdogs.presentation.thisPlayer
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class KtorRealtimeMessagingClient(
    private val client: HttpClient
): RealtimeMessagingClient {

    private var session: WebSocketSession? = null

    override fun getGameStateStream(): Flow<GameState> {
        return flow {
            session = client.webSocketSession {
                url("ws://192.168.0.186:8080/play/socket")
            }
            val gameStates = session!!
                .incoming
                .consumeAsFlow()
                .filterIsInstance<Frame.Text>()
                .mapNotNull {
                    val text = it.readText()

                    // Special case for the first message
                    if(text.contains("playerName")) {
                        thisPlayer = Json.decodeFromString<Player>(text).playerName
                        GameState()
                    }
                    else
                        Json.decodeFromString<GameState>(text)
                }

            emitAll(gameStates)
        }
    }

    override suspend fun sendAction(action: MakeTurn) {
        session?.outgoing?.send(
            Frame.Text("make_turn#${Json.encodeToString(action)}")
        )
    }

    override suspend fun close() {
        session?.close()
        session = null
    }
}