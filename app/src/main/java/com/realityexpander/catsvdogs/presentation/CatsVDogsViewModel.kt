package com.realityexpander.catsvdogs.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.catsvdogs.data.GameState
import com.realityexpander.catsvdogs.data.MakeTurn
import com.realityexpander.catsvdogs.data.IRealtimeMessagingClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.net.ConnectException
import javax.inject.Inject

var thisPlayer: String = "X"

@Serializable
data class Player(val playerName: String, val id: String)

@HiltViewModel
class CatsVDogsViewModel @Inject constructor(
    private val client: IRealtimeMessagingClient
): ViewModel() {


    val state = client
        .getGameStateFlow()
        .onStart { _isConnecting.value = true }
        .onEach { gameState ->
            _isConnecting.value = false
            println("Received state: $gameState")
        }
        .catch { t -> _showConnectionError.value = t is ConnectException }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GameState())

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    private val _showConnectionError = MutableStateFlow(false)
    val showConnectionError = _showConnectionError.asStateFlow()

    fun finishTurn(x: Int, y: Int) {
        if(state.value.field[y][x] != null || state.value.winningPlayer != null) {
            return
        }

        viewModelScope.launch {
            client.sendAction(MakeTurn(x, y))
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            client.close()
        }
    }
}