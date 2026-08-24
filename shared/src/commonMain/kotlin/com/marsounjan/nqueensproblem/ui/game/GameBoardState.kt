package com.marsounjan.nqueensproblem.ui.game

import androidx.compose.runtime.Immutable
import kotlin.math.abs

@Immutable
data class GameBoardState(
    val boardSize: Int,
    val queens: Set<GameBoardPosition> = emptySet(),
) {

    val queensRemaining: Int = boardSize - queens.size

    val conflictingQueens: Set<GameBoardPosition> = buildSet {
        val placed = queens.toList()
        for (i in placed.indices) {
            for (j in i + 1 until placed.size) {
                if (threaten(placed[i], placed[j])) {
                    add(placed[i])
                    add(placed[j])
                }
            }
        }
    }

    val isSolved: Boolean = queens.size == boardSize && conflictingQueens.isEmpty()

    /**
     * Tapping an occupied cell removes that queen. Tapping an empty cell places a queen
     * there, unless [boardSize] queens are already on the board - the puzzle is exactly
     * "place n queens", so extra taps beyond that are no-ops rather than overflowing.
     */
    fun toggle(position: GameBoardPosition): GameBoardState = when {
        position in queens -> copy(queens = queens - position)
        queens.size < boardSize -> copy(queens = queens + position)
        else -> this
    }

    private fun threaten(a: GameBoardPosition, b: GameBoardPosition): Boolean =
        a.row == b.row || a.col == b.col || abs(a.row - b.row) == abs(a.col - b.col)
}
