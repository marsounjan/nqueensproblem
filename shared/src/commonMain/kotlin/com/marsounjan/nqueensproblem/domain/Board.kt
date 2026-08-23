package com.marsounjan.nqueensproblem.domain

import kotlin.math.abs

/**
 * Immutable N-Queens board. Conflicts follow the classic N-Queens rule: two queens
 * threaten each other if they share a row, column, or diagonal - there is no notion
 * of one queen blocking another queen's line, unlike a real chess move.
 */
data class Board(
    val size: Int,
    val queens: Set<Position> = emptySet(),
) {

    val queensRemaining: Int get() = size - queens.size

    val isSolved: Boolean get() = queens.size == size && conflictingQueens().isEmpty()

    /**
     * Tapping an occupied cell removes that queen. Tapping an empty cell places a queen
     * there, unless [size] queens are already on the board - the puzzle is exactly
     * "place n queens", so extra taps beyond that are no-ops rather than overflowing.
     */
    fun toggle(position: Position): Board = when {
        position in queens -> copy(queens = queens - position)
        queens.size < size -> copy(queens = queens + position)
        else -> this
    }

    fun conflictingQueens(): Set<Position> {
        val conflicting = mutableSetOf<Position>()
        val placed = queens.toList()
        for (i in placed.indices) {
            for (j in i + 1 until placed.size) {
                if (threaten(placed[i], placed[j])) {
                    conflicting += placed[i]
                    conflicting += placed[j]
                }
            }
        }
        return conflicting
    }

    /**
     * Every square that lies on some placed queen's row, column, or diagonal (excluding
     * that queen's own square), regardless of whether that queen currently conflicts with
     * another one. Used to shade squares the player shouldn't place on next.
     */
    fun attackedSquares(): Set<Position> {
        val attacked = mutableSetOf<Position>()
        for (queen in queens) {
            attacked += squaresAttackedBy(queen)
        }
        return attacked
    }

    private fun threaten(a: Position, b: Position): Boolean =
        a.row == b.row || a.col == b.col || abs(a.row - b.row) == abs(a.col - b.col)

    private fun squaresAttackedBy(queen: Position): Set<Position> {
        val result = mutableSetOf<Position>()
        for (i in 0 until size) {
            if (i != queen.row) result += Position(i, queen.col)
            if (i != queen.col) result += Position(queen.row, i)
        }
        addDiagonal(result, queen, rowStep = -1, colStep = -1)
        addDiagonal(result, queen, rowStep = -1, colStep = 1)
        addDiagonal(result, queen, rowStep = 1, colStep = -1)
        addDiagonal(result, queen, rowStep = 1, colStep = 1)
        return result
    }

    private fun addDiagonal(into: MutableSet<Position>, from: Position, rowStep: Int, colStep: Int) {
        var row = from.row + rowStep
        var col = from.col + colStep
        while (row in 0 until size && col in 0 until size) {
            into += Position(row, col)
            row += rowStep
            col += colStep
        }
    }
}
