package com.marsounjan.nqueensproblem.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.marsounjan.nqueensproblem.util.formatGameTime
import nqueensproblem.shared.generated.resources.Res
import nqueensproblem.shared.generated.resources.win_new_best
import nqueensproblem.shared.generated.resources.win_ok
import nqueensproblem.shared.generated.resources.win_time
import nqueensproblem.shared.generated.resources.win_title
import org.jetbrains.compose.resources.stringResource

data class WinDialogModel(
    val elapsedMillis : Long,
    val isNewBest: Boolean,
)

@Composable
fun WinDialog(
    model: WinDialogModel,
    onConfirmClicked: () -> Unit,
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
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(stringResource(Res.string.win_time, formatGameTime(model.elapsedMillis)))
                if (model.isNewBest) {
                    Text(stringResource(Res.string.win_new_best), style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirmClicked) { Text(stringResource(Res.string.win_ok)) }
        }
    )
}