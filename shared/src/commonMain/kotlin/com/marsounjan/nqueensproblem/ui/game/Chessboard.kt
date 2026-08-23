package com.marsounjan.nqueensproblem.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.marsounjan.nqueensproblem.domain.Position
import com.marsounjan.nqueensproblem.ui.theme.NQueensTheme
import org.jetbrains.compose.resources.stringResource
import nqueensproblem.shared.generated.resources.Res
import nqueensproblem.shared.generated.resources.cell_empty
import nqueensproblem.shared.generated.resources.cell_empty_attacked
import nqueensproblem.shared.generated.resources.cell_queen
import nqueensproblem.shared.generated.resources.cell_queen_conflicting

private val MIN_CELL_SIZE = 32.dp

@Composable
fun Chessboard(
    boardSize: Int,
    queens: Set<Position>,
    conflictingQueens: Set<Position>,
    attackedSquares: Set<Position>,
    onCellTap: (Position) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val availableSize = min(maxWidth, maxHeight)
        val cellSize = maxOf(availableSize / boardSize, MIN_CELL_SIZE)
        val boardPixelSize = cellSize * boardSize

        Column(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())
                .size(boardPixelSize),
        ) {
            for (row in 0 until boardSize) {
                Row {
                    for (col in 0 until boardSize) {
                        val position = remember(row, col) { Position(row, col) }
                        BoardCell(
                            cellSize = cellSize,
                            isDarkSquare = (row + col) % 2 == 1,
                            hasQueen = position in queens,
                            isConflicting = position in conflictingQueens,
                            isAttacked = position in attackedSquares,
                            onTap = { onCellTap(position) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCell(
    cellSize: Dp,
    isDarkSquare: Boolean,
    hasQueen: Boolean,
    isConflicting: Boolean,
    isAttacked: Boolean,
    onTap: () -> Unit,
) {
    val colors = NQueensTheme.colors
    val baseColor = if (isDarkSquare) colors.darkSquare else colors.lightSquare
    val targetColor = if (isConflicting) colors.conflictSquare else baseColor
    val animatedColor by animateColorAsState(targetColor)
    val description = stringResource(
        when {
            isConflicting -> Res.string.cell_queen_conflicting
            hasQueen -> Res.string.cell_queen
            isAttacked -> Res.string.cell_empty_attacked
            else -> Res.string.cell_empty
        },
    )

    Box(
        modifier = Modifier
            .size(cellSize)
            .background(animatedColor)
            .clickable(onClick = onTap)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (hasQueen) {
            val queenColor by animateColorAsState(
                if (isConflicting) colors.conflictQueen else colors.queen,
            )
            val fontSize = with(LocalDensity.current) { (cellSize.toPx() * 0.6f).toSp() }
            Text(text = "♛", color = queenColor, fontSize = fontSize)
        }
    }
}
