package com.marsounjan.nqueensproblem.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.marsounjan.nqueensproblem.util.formatGameTime
import org.jetbrains.compose.resources.stringResource
import nqueensproblem.shared.generated.resources.Res
import nqueensproblem.shared.generated.resources.app_title
import nqueensproblem.shared.generated.resources.setup_best_time_known
import nqueensproblem.shared.generated.resources.setup_no_record
import nqueensproblem.shared.generated.resources.setup_board_size_label
import nqueensproblem.shared.generated.resources.setup_decrease_board_size
import nqueensproblem.shared.generated.resources.setup_increase_board_size
import nqueensproblem.shared.generated.resources.setup_start

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel,
    modifier : Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(Res.string.app_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        val bestTimeSeconds = state.bestTimeSeconds
        Text(
            text = if (bestTimeSeconds != null) {
                stringResource(Res.string.setup_best_time_known, state.boardSize,
                    formatGameTime(bestTimeSeconds)
                )
            } else {
                stringResource(Res.string.setup_no_record, state.boardSize)
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            val decreaseDescription = stringResource(Res.string.setup_decrease_board_size)
            FilledIconButton(
                onClick = { viewModel.onBoardSizeChanged(state.boardSize - 1) },
                enabled = state.boardSize > state.minBoardSize,
                modifier = Modifier.semantics { contentDescription = decreaseDescription },
            ) { Text("−", style = MaterialTheme.typography.titleLarge) }

            Text(stringResource(Res.string.setup_board_size_label, state.boardSize), style = MaterialTheme.typography.headlineSmall)

            val increaseDescription = stringResource(Res.string.setup_increase_board_size)
            FilledIconButton(
                onClick = { viewModel.onBoardSizeChanged(state.boardSize + 1) },
                enabled = state.boardSize < state.maxBoardSize,
                modifier = Modifier.semantics { contentDescription = increaseDescription },
            ) { Text("+", style = MaterialTheme.typography.titleLarge) }
        }

        Spacer(Modifier.height(32.dp))
        Button(onClick = { viewModel.onStartClicked() }) {
            Text(stringResource(Res.string.setup_start))
        }
    }
}
