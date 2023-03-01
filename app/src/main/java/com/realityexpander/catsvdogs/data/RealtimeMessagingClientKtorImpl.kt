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
                    println("gameStates: frame= $it")
                }
                .mapNotNull { frame ->
                    val frameText = frame.readText()

                    // Special case for the first message (user player X or O)
                    if(frameText.contains("playerName")) {
                        thisPlayer = Json.decodeFromString<Player>(frameText).playerName
                        GameState()
                    }
                    else if(frameText.contains("ping")) {
                        //println("Received ping")
                        session?.outgoing?.send(Frame.Text("pong"))
                        null
                    }
                    else {
                        // Decode the frame text into a GameState object
                        Json.decodeFromString<GameState>(frameText)
                    }
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

            //println("Sending ping...(1) ")
            yield()

            try {
                if ((pingSession as DefaultClientWebSocketSession).isActive) {
                    yield()

                    // Launch this in its own coroutine so we can perform timeout.
                    // Note: This should be handled by the ktor client, but it doesn't seem to be working.
                    // This is a workaround.
                    //   See:
                    // https://youtrack.jetbrains.com/issue/KTOR-5418
                    // https://youtrack.jetbrains.com/issue/KTOR-3504
                    // https://youtrack.jetbrains.com/issue/KTOR-2504
                    var isFinished = false
                    CoroutineScope(Dispatchers.Main).launch {
                        pingSession?.outgoing?.send(  // this may freeze if server is dead (unknown reason)
                            Frame.Text("ping")
                        )
                        isFinished = true
                    }

                    var retryCount = 0
                    while (
                        !isFinished
//                    && (pingSession as DefaultClientWebSocketSession).isActive
//                    && pingSession?.incoming?.isEmpty == false
//                    && pingSession?.incoming?.receiveOrNull() != null
                        && retryCount < 10
                    ) {
                        delay(250)
                        retryCount++
                    }

                    if(retryCount >= 10) {
                        println("Sending Ping - Retry Count Exceeded - connection is dead")
                        throw ConnectException("Server is down - send ping failed - retry count exceeded")
                    }

                    //println("Sending ping. (2) Success")
                } else {
                    println("Sending ping. connection is unresponsive")
                    throw ConnectException("Server is down - send ping failed")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Sending ping EXCEPTION ${e.localizedMessage}")
                throw ConnectException("Server is down - ${e.localizedMessage}")
            }

            onSuccess()
            delay(1000)

            // Check for return pong
            withTimeout(1000) {
                if (pingSession?.incoming?.isEmpty == true) {
                    pingSession?.close()
                    pingSession = null
                    throw ConnectException("Server is down")
                }
                //println("Connection is alive")
            }
        }
    }

    override suspend fun close() {
        session?.close()
        session = null
    }
}