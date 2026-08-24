package com.marsounjan.nqueensproblem.ui.game

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.marsounjan.nqueensproblem.util.formatGameTime
import com.marsounjan.nqueensproblem.ui.theme.NQueensTheme
import nqueensproblem.shared.generated.resources.Res
import nqueensproblem.shared.generated.resources.game_best_time
import nqueensproblem.shared.generated.resources.game_no_record
import nqueensproblem.shared.generated.resources.game_queens_left
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().safeContentPadding()
    ) {
        GameTopBar(state = state)
        Spacer(Modifier.height(8.dp))
        GameBoard(
            state = state.boardState,
            onCellTap = viewModel::cellTapped,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        QueensRemainingIndicator(remaining = state.boardState.queensRemaining)
    }

    state.winDialog?.let {
        WinDialog(
            model = it,
            onConfirmClicked = viewModel::winDialogConfirmClicked,
        )
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.screenResumed() }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { viewModel.screenPaused() }
}

@Composable
private fun GameTopBar(state: GameUiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatGameTime(state.elapsedMillis),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        val bestTimeSeconds = state.bestTimeMillis
        val bestTimeText = if (bestTimeSeconds != null) {
            stringResource(Res.string.game_best_time, formatGameTime(bestTimeSeconds))
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
        Text(
            stringResource(Res.string.game_queens_left, remaining),
            style = MaterialTheme.typography.bodyLarge
        )
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
