package com.realityexpander.catsvdogs.data

import com.realityexpander.catsvdogs.CatsVDogsApp
import com.realityexpander.catsvdogs.presentation.Player
import com.realityexpander.catsvdogs.presentation.thisPlayer
import io.ktor.client.*
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.*
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.ConnectException

class RealtimeMessagingClientKtorImpl(
    private val client: HttpClient
): IRealtimeMessagingClient {

    private var session: WebSocketSession? = null
    private var pingSession: WebSocketSession? = null

    override fun getGameStateFlow(): Flow<GameState> {
        return flow {
            session = client.webSocketSession {
                url("ws://192.168.0.186:8080/play/socket/${CatsVDogsApp.userId}")
            }

            val gameStates = session!!
                .incoming
                .consumeAsFlow()
                .filterIsInstance<Frame.Text>()
                .also {
                    println("gameStates: $it")
                }
                .mapNotNull { frame ->
                    val frameText = frame.readText()

                    // Special case for the first message (user player X or O)
                    if(frameText.contains("playerName")) {
                        thisPlayer = Json.decodeFromString<Player>(frameText).playerName
                        GameState()
                    }
                    else
                        Json.decodeFromString<GameState>(frameText)
                }
//                .retryWhen { cause, attempt ->  // Retry on error
                    //println(".retryWhen cause: ${cause.localizedMessage}")
//                    println(".retryWhen Attempt: $attempt")
//                    true
//                }
                .catch { cause ->  // Catch error
                    println(".catch Error: $cause")
                }


            emitAll(gameStates)
        }
    }

    override suspend fun sendAction(action: MakeTurn) {
        session?.outgoing?.send(
            Frame.Text("make_turn#${Json.encodeToString(action)}")
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun handlePings(
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        pingSession = client.webSocketSession {
            url("ws://192.168.0.186:8080/echo")
        }

        while(true) {
            var isFinished = false
            var retryCount = 0

            println("Sending ping...")
            yield()

            // check connection


            try {
                if ((pingSession as DefaultClientWebSocketSession).isActive) {
                    yield()
                    CoroutineScope(Dispatchers.Main).launch {
                        println("Sending ping... (1)")
                        pingSession?.outgoing?.send(
                            Frame.Text("ping")
                        )
                        isFinished = true
                    }
//                pingSession?.outgoing?.send(
//                    Frame.Text("ping")
//                )
                    retryCount = 0
                    while (
                        !isFinished
//                    && (pingSession as DefaultClientWebSocketSession).isActive
//                    && pingSession?.incoming?.isEmpty == false
//                    && pingSession?.incoming?.receiveOrNull() != null
                        && retryCount < 10
                    ) {
                        delay(250)
                        retryCount++
                        println("Sending ping... (1) - retryCount: $retryCount")
                    }

                    if(retryCount >= 10) {
                        println("Sending Ping - Retry Count Exceeded - connection is dead")
                        throw ConnectException("Server is down - send ping failed - retry count exceeded")
                    }

                    println("Sending ping... (2) Success")
                } else {
                    println("Sending ping... connection is dead")
                    throw ConnectException("Server is down - send ping failed")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Sending ping EXCEPTION ${e.localizedMessage}")
                throw ConnectException("Server is down - ${e.localizedMessage}")
            }


//            withTimeout(1000) {
//                if((pingSession as DefaultClientWebSocketSession).isActive) {
//                    yield()
//                    pingSession?.outgoing?.send(
//                        Frame.Text("ping")
//                    )
//                    println("Sending ping... (2)")
//                } else {
//                    println("Sending ping... connection is dead")
//                    throw ConnectException("Server is down - send ping failed")
//                }
//            }
            onSuccess()
            delay(1000)

            // Check if connection is still alive
            withTimeout(1000) {

                if (pingSession?.incoming?.isEmpty == true) {
                    println("Connection is dead")
                    pingSession?.close()
                    pingSession = null
                    throw ConnectException("Server is down")
                }
                println("Connection is alive")
            }
        }
    }

    override suspend fun close() {
        session?.close()
        session = null
    }
}