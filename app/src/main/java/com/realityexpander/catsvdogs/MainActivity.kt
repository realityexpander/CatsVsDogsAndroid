package com.realityexpander.catsvdogs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.realityexpander.catsvdogs.presentation.CatsVDogsField
import com.realityexpander.catsvdogs.presentation.CatsVDogsViewModel
import com.realityexpander.catsvdogs.presentation.thisPlayer
import com.realityexpander.catsvdogs.ui.theme.CatsVDogsTheme
import dagger.hilt.android.AndroidEntryPoint

// X = Cats 🐈
// O = Dogs 🐕

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CatsVDogsTheme {
                val viewModel = hiltViewModel<CatsVDogsViewModel>()
                val state by viewModel.state.collectAsState()
                val isConnecting by viewModel.isConnecting.collectAsState()
                val showConnectionError by viewModel.showConnectionError.collectAsState()
                
                if(showConnectionError) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Couldn't connect to the server",
                            color = MaterialTheme.colors.error
                        )
                    }
                    return@CatsVDogsTheme
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        if(!state.connectedPlayers.contains('X')) {
                            Text(
                                text = "Waiting for player 🐈 Cats",
                                fontSize = 32.sp
                            )
                        } else if(!state.connectedPlayers.contains('O')) {
                            Text(
                                text = "Waiting for player 🐕 Dogs",
                                fontSize = 32.sp
                            )
                        }
                    }
                    if(
                        state.connectedPlayers.size == 2 && state.winningPlayer == null &&
                                !state.isBoardFull
                    ) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                        ) {
                            Text(
                                text = if (thisPlayer == "X")
                                    "You Are Cats 🐈"
                                else
                                    "You are Dogs 🐕",
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = if (state.playerAtTurn == 'O') {
                                    if (thisPlayer == state.playerAtTurn.toString())
                                        "Your Turn"
                                    else
                                        "Dogs 🐕 Turn"
                                } else {
                                    if(thisPlayer == state.playerAtTurn.toString())
                                        "Your Turn"
                                    else
                                        "Cats 🐈 Turn"
                                },
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    CatsVDogsField(
                        state = state,
                        onTapInField = viewModel::finishTurn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(16.dp)
                    )

                    if(state.isBoardFull || state.winningPlayer != null) {
                        Text(
                            text = when(state.winningPlayer) {
                                'X' -> "Player Cats 🐈 won!"
                                'O' -> "Player Dogs 🐕 won!"
                                else -> "It's a draw!"
                            },
                            fontSize = 32.sp,
                            modifier = Modifier
                                .padding(bottom = 32.dp)
                                .align(Alignment.BottomCenter)
                        )
                    }

                    if(isConnecting) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}