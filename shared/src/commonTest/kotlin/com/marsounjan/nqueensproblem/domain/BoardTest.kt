package com.marsounjan.nqueensproblem.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardTest {

    @Test
    fun newBoard_isEmpty() {
        val board = Board(size = 8)
        assertTrue(board.queens.isEmpty())
        assertEquals(8, board.queensRemaining)
        assertFalse(board.isSolved)
    }

    @Test
    fun toggle_onEmptyCell_placesQueen() {
        val board = Board(size = 8).toggle(Position(0, 0))
        assertEquals(setOf(Position(0, 0)), board.queens)
    }

    @Test
    fun toggle_onOccupiedCell_removesQueen() {
        val board = Board(size = 8).toggle(Position(0, 0)).toggle(Position(0, 0))
        assertTrue(board.queens.isEmpty())
    }

    @Test
    fun toggle_beyondSize_isNoOp() {
        // Place non-conflicting queens up to the cap, then try to place one more.
        var board = Board(size = 4)
        listOf(Position(0, 1), Position(1, 3), Position(2, 0), Position(3, 2)).forEach {
            board = board.toggle(it)
        }
        assertEquals(4, board.queens.size)

        val afterExtraTap = board.toggle(Position(0, 0))
        assertEquals(board.queens, afterExtraTap.queens)
    }

    @Test
    fun toggle_beyondSize_stillAllowsRemoval() {
        var board = Board(size = 4)
        val placements = listOf(Position(0, 1), Position(1, 3), Position(2, 0), Position(3, 2))
        placements.forEach { board = board.toggle(it) }

        board = board.toggle(placements.first())
        assertEquals(3, board.queens.size)
        assertFalse(placements.first() in board.queens)
    }

    @Test
    fun conflictingQueens_sameRow() {
        val board = Board(size = 4, queens = setOf(Position(0, 0), Position(0, 3)))
        assertEquals(setOf(Position(0, 0), Position(0, 3)), board.conflictingQueens())
    }

    @Test
    fun conflictingQueens_sameColumn() {
        val board = Board(size = 4, queens = setOf(Position(0, 2), Position(3, 2)))
        assertEquals(setOf(Position(0, 2), Position(3, 2)), board.conflictingQueens())
    }

    @Test
    fun conflictingQueens_mainDiagonal() {
        val board = Board(size = 4, queens = setOf(Position(0, 0), Position(3, 3)))
        assertEquals(setOf(Position(0, 0), Position(3, 3)), board.conflictingQueens())
    }

    @Test
    fun conflictingQueens_antiDiagonal() {
        val board = Board(size = 4, queens = setOf(Position(0, 3), Position(3, 0)))
        assertEquals(setOf(Position(0, 3), Position(3, 0)), board.conflictingQueens())
    }

    @Test
    fun conflictingQueens_nonAdjacentDiagonal_stillConflicts() {
        // No blocking semantics: (0,0) and (2,2) conflict even though (1,1) is between them.
        val board = Board(size = 4, queens = setOf(Position(0, 0), Position(1, 1), Position(2, 2)))
        assertEquals(setOf(Position(0, 0), Position(1, 1), Position(2, 2)), board.conflictingQueens())
    }

    @Test
    fun conflictingQueens_onlyFlagsQueensActuallyInvolved() {
        val safe = Position(1, 3)
        val board = Board(size = 4, queens = setOf(Position(0, 0), Position(0, 1), safe))
        val conflicting = board.conflictingQueens()
        assertTrue(Position(0, 0) in conflicting)
        assertTrue(Position(0, 1) in conflicting)
        assertFalse(safe in conflicting)
    }

    @Test
    fun conflictingQueens_knownFourQueensSolution_hasNoConflicts() {
        val board = Board(size = 4, queens = setOf(Position(0, 1), Position(1, 3), Position(2, 0), Position(3, 2)))
        assertTrue(board.conflictingQueens().isEmpty())
        assertTrue(board.isSolved)
    }

    @Test
    fun attackedSquares_singleQueen_coversFullRowColumnAndBothDiagonals_butNotItself() {
        val board = Board(size = 4, queens = setOf(Position(1, 1)))
        val attacked = board.attackedSquares()

        assertFalse(Position(1, 1) in attacked)
        // row
        assertTrue(Position(1, 0) in attacked)
        assertTrue(Position(1, 3) in attacked)
        // column
        assertTrue(Position(0, 1) in attacked)
        assertTrue(Position(3, 1) in attacked)
        // main diagonal
        assertTrue(Position(0, 0) in attacked)
        assertTrue(Position(2, 2) in attacked)
        assertTrue(Position(3, 3) in attacked)
        // anti-diagonal
        assertTrue(Position(0, 2) in attacked)
        assertTrue(Position(2, 0) in attacked)
        // unrelated square
        assertFalse(Position(3, 0) in attacked)
    }

    @Test
    fun attackedSquares_isNotLimitedToConflictingQueens() {
        // A single, non-conflicting queen still shades the squares it threatens.
        val board = Board(size = 4, queens = setOf(Position(0, 0)))
        assertTrue(board.conflictingQueens().isEmpty())
        assertTrue(Position(0, 3) in board.attackedSquares())
        assertTrue(Position(3, 3) in board.attackedSquares())
    }

    @Test
    fun attackedSquares_corner_doesNotWrapAround() {
        val board = Board(size = 4, queens = setOf(Position(0, 0)))
        val attacked = board.attackedSquares()
        // Nothing "wraps" past the edges - e.g. no negative-index style wraparound artifacts.
        assertEquals(4 - 1 + 4 - 1 + 3, attacked.size) // row + col (minus double count at (0,0), excluded) + one diagonal
    }

    @Test
    fun isSolved_requiresExactlyNQueensAndNoConflicts() {
        val tooFew = Board(size = 4, queens = setOf(Position(0, 1)))
        assertFalse(tooFew.isSolved)

        val fullButConflicting = Board(size = 4, queens = setOf(Position(0, 0), Position(0, 1), Position(1, 2), Position(2, 3)))
        assertEquals(4, fullButConflicting.queens.size)
        assertFalse(fullButConflicting.isSolved)

        val solved = Board(size = 4, queens = setOf(Position(0, 1), Position(1, 3), Position(2, 0), Position(3, 2)))
        assertTrue(solved.isSolved)
    }
}
