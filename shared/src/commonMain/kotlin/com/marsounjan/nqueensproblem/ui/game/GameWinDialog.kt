package com.marsounjan.nqueensproblem.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp
import com.marsounjan.nqueensproblem.util.formatGameTime
import nqueensproblem.shared.generated.resources.Res
import nqueensproblem.shared.generated.resources.win_new_best
import nqueensproblem.shared.generated.resources.win_ok
import nqueensproblem.shared.generated.resources.win_time
import nqueensproblem.shared.generated.resources.win_title
import org.jetbrains.compose.resources.stringResource

@Immutable
data class WinDialogModel(
    val elapsedMillis: Long,
    val isNewBest: Boolean,
)

@Composable
fun WinDialog(
    model: WinDialogModel,
    onConfirmClicked: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(Res.string.win_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(stringResource(Res.string.win_time, formatGameTime(model.elapsedMillis)))
                if (model.isNewBest) {
                    Text(
                        stringResource(Res.string.win_new_best),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirmClicked) { Text(stringResource(Res.string.win_ok)) }
        }
    )
}