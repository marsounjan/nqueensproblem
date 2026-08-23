package com.marsounjan.nqueensproblem.ui.game

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.marsounjan.nqueensproblem.ui.navigation.Navigator
import com.marsounjan.nqueensproblem.ui.formatElapsedSeconds
import com.marsounjan.nqueensproblem.ui.theme.NQueensTheme
import org.jetbrains.compose.resources.stringResource
import nqueensproblem.shared.generated.resources.Res
import nqueensproblem.shared.generated.resources.game_best_time
import nqueensproblem.shared.generated.resources.game_no_record
import nqueensproblem.shared.generated.resources.game_queens_left
import nqueensproblem.shared.generated.resources.game_reset
import nqueensproblem.shared.generated.resources.win_change_size
import nqueensproblem.shared.generated.resources.win_new_best
import nqueensproblem.shared.generated.resources.win_play_again
import nqueensproblem.shared.generated.resources.win_time
import nqueensproblem.shared.generated.resources.win_title

@Composable
fun GameScreen(viewModel: GameViewModel, navigator: Navigator) {
    val state by viewModel.uiState.collectAsState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.screenResumed() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.screenPaused() }

    Column(modifier = Modifier.fillMaxSize().safeContentPadding()) {
        GameTopBar(state = state, onReset = viewModel::reset)
        Spacer(Modifier.height(8.dp))
        Chessboard(
            boardSize = state.boardSize,
            queens = state.queens,
            conflictingQueens = state.conflictingQueens,
            attackedSquares = state.attackedSquares,
            onCellTap = viewModel::tapCell,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        QueensRemainingIndicator(remaining = state.queensRemaining)
    }

    if (state.isSolved) {
        WinDialog(
            state = state,
            onPlayAgain = viewModel::reset,
            onChangeSize = { viewModel.onChangeSizeClicked(navigator) },
        )
    }
}

@Composable
private fun GameTopBar(state: GameUiState, onReset: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(formatElapsedSeconds(state.elapsedSeconds), style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = onReset) { Text(stringResource(Res.string.game_reset)) }
        }
        val bestTimeSeconds = state.bestTimeSeconds
        val bestTimeText = if (bestTimeSeconds != null) {
            stringResource(Res.string.game_best_time, formatElapsedSeconds(bestTimeSeconds))
        } else {
            stringResource(Res.string.game_no_record)
        }
        Text(bestTimeText, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun QueensRemainingIndicator(remaining: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(Res.string.game_queens_left, remaining), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(4.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        ) {
            repeat(remaining) {
                Text("♛", fontSize = 24.sp, color = NQueensTheme.colors.queen)
            }
        }
    }
}

@Composable
private fun WinDialog(
    state: GameUiState,
    onPlayAgain: () -> Unit,
    onChangeSize: () -> Unit,
) {
    val scale = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    AlertDialog(
        onDismissRequest = {},
        modifier = Modifier.scale(scale.value),
        title = { Text(stringResource(Res.string.win_title)) },
        text = {
            Column {
                Text(stringResource(Res.string.win_time, formatElapsedSeconds(state.elapsedSeconds)))
                if (state.isNewBest) {
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(Res.string.win_new_best), style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onPlayAgain) { Text(stringResource(Res.string.win_play_again)) }
        },
        dismissButton = {
            TextButton(onClick = onChangeSize) { Text(stringResource(Res.string.win_change_size)) }
        },
    )
}
