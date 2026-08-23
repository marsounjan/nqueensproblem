package com.marsounjan.nqueensproblem.presentation

import androidx.lifecycle.SavedStateHandle
import com.marsounjan.nqueensproblem.domain.Position
import com.marsounjan.nqueensproblem.testing.FakeBestTimesRepository
import com.marsounjan.nqueensproblem.testing.FakeNavigator
import com.marsounjan.nqueensproblem.testing.FakeSoundPlayer
import com.marsounjan.nqueensproblem.testing.ViewModelTest
import com.marsounjan.nqueensproblem.ui.game.GameViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// A known, non-conflicting solution for a 4x4 board.
private val FOUR_QUEENS_SOLUTION = listOf(Position(0, 1), Position(1, 3), Position(2, 0), Position(3, 2))

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest : ViewModelTest() {

    private fun createViewModel(
        boardSize: Int = 4,
        repository: FakeBestTimesRepository = FakeBestTimesRepository(),
        soundPlayer: FakeSoundPlayer = FakeSoundPlayer(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = GameViewModel(boardSize, repository, soundPlayer, savedStateHandle)

    @Test
    fun initialState_isAnEmptyBoardWithNoElapsedTime() = runTest(testDispatcher) {
        val viewModel = createViewModel(boardSize = 4)
        val state = viewModel.uiState.value

        assertEquals(4, state.boardSize)
        assertTrue(state.queens.isEmpty())
        assertEquals(4, state.queensRemaining)
        assertEquals(0, state.elapsedSeconds)
        assertFalse(state.isSolved)
    }

    @Test
    fun tapCell_onEmptySquare_placesQueenAndPlaysSafeSound() = runTest(testDispatcher) {
        val soundPlayer = FakeSoundPlayer()
        val viewModel = createViewModel(soundPlayer = soundPlayer)

        viewModel.tapCell(Position(0, 1))

        assertEquals(setOf(Position(0, 1)), viewModel.uiState.value.queens)
        assertEquals(1, soundPlayer.safePlacementCount)
        assertEquals(0, soundPlayer.conflictPlacementCount)
    }

    @Test
    fun tapCell_creatingAConflict_playsConflictSound() = runTest(testDispatcher) {
        val soundPlayer = FakeSoundPlayer()
        val viewModel = createViewModel(soundPlayer = soundPlayer)

        viewModel.tapCell(Position(0, 0))
        viewModel.tapCell(Position(0, 1)) // same row as the first queen

        assertTrue(Position(0, 1) in viewModel.uiState.value.conflictingQueens)
        assertEquals(1, soundPlayer.safePlacementCount)
        assertEquals(1, soundPlayer.conflictPlacementCount)
    }

    @Test
    fun tapCell_onOccupiedSquare_removesQueen_withoutPlayingASound() = runTest(testDispatcher) {
        val soundPlayer = FakeSoundPlayer()
        val viewModel = createViewModel(soundPlayer = soundPlayer)

        viewModel.tapCell(Position(0, 0))
        viewModel.tapCell(Position(0, 0))

        assertTrue(viewModel.uiState.value.queens.isEmpty())
        assertEquals(1, soundPlayer.safePlacementCount)
        assertEquals(0, soundPlayer.conflictPlacementCount)
    }

    @Test
    fun screenResumed_ticksElapsedTimeEverySecond() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.screenResumed()

        advanceTimeBy(3_500)
        runCurrent()

        assertEquals(3, viewModel.uiState.value.elapsedSeconds)
        viewModel.screenPaused() // stop the ticker so the test scheduler can go idle
    }

    @Test
    fun screenPaused_stopsTheTimer() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        viewModel.screenResumed()
        advanceTimeBy(2_000)
        runCurrent()

        viewModel.screenPaused()
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(2, viewModel.uiState.value.elapsedSeconds)
    }

    @Test
    fun solvingTheBoard_stopsTimer_playsWinSound_andReportsTime() = runTest(testDispatcher) {
        val repository = FakeBestTimesRepository()
        val soundPlayer = FakeSoundPlayer()
        val viewModel = createViewModel(repository = repository, soundPlayer = soundPlayer)

        viewModel.screenResumed()
        advanceTimeBy(4_000)
        runCurrent()

        FOUR_QUEENS_SOLUTION.forEach { viewModel.tapCell(it) }
        runCurrent()

        val state = viewModel.uiState.value
        assertTrue(state.isSolved)
        assertEquals(1, soundPlayer.winCount)
        assertEquals(listOf(4 to 4L), repository.reportedSolves)
        assertTrue(state.isNewBest)

        // Timer no longer runs after solving.
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(4, viewModel.uiState.value.elapsedSeconds)
    }

    @Test
    fun solvingSlowerThanExistingBest_isNotFlaggedAsNewBest() = runTest(testDispatcher) {
        val repository = FakeBestTimesRepository().apply { seedBestTime(boardSize = 4, elapsedSeconds = 2) }
        val viewModel = createViewModel(repository = repository)

        viewModel.screenResumed()
        advanceTimeBy(4_000)
        runCurrent()

        FOUR_QUEENS_SOLUTION.forEach { viewModel.tapCell(it) }
        runCurrent()

        assertFalse(viewModel.uiState.value.isNewBest)
        assertEquals(2, viewModel.uiState.value.bestTimeSeconds) // unbeaten best stays
    }

    @Test
    fun tapCell_afterSolved_isIgnored() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        FOUR_QUEENS_SOLUTION.forEach { viewModel.tapCell(it) }
        runCurrent()

        val solvedQueens = viewModel.uiState.value.queens
        viewModel.tapCell(Position(0, 0))

        assertEquals(solvedQueens, viewModel.uiState.value.queens)
    }

    @Test
    fun reset_clearsBoardAndTimer_andAllowsANewSolve() = runTest(testDispatcher) {
        val soundPlayer = FakeSoundPlayer()
        val viewModel = createViewModel(soundPlayer = soundPlayer)

        viewModel.screenResumed()
        advanceTimeBy(3_000)
        runCurrent()
        viewModel.tapCell(Position(0, 0))

        viewModel.reset()

        val state = viewModel.uiState.value
        assertTrue(state.queens.isEmpty())
        assertEquals(0, state.elapsedSeconds)
        assertFalse(state.isSolved)
        assertFalse(state.isNewBest)

        // Timer was paused by reset(), so time shouldn't advance until resume() is called again.
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(0, viewModel.uiState.value.elapsedSeconds)
    }

    @Test
    fun gameState_survivesRecreationFromTheSameSavedStateHandle() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = createViewModel(savedStateHandle = savedStateHandle)
        firstViewModel.screenResumed()
        advanceTimeBy(6_000)
        runCurrent()
        firstViewModel.tapCell(Position(0, 1))
        firstViewModel.screenPaused()

        val recreatedViewModel = createViewModel(savedStateHandle = savedStateHandle)
        val state = recreatedViewModel.uiState.value

        assertEquals(setOf(Position(0, 1)), state.queens)
        assertEquals(6, state.elapsedSeconds)
    }

    @Test
    fun onChangeSizeClicked_navigatesBack() = runTest(testDispatcher) {
        val navigator = FakeNavigator()
        val viewModel = createViewModel()

        viewModel.onChangeSizeClicked(navigator)

        assertEquals(1, navigator.goBackCallCount)
    }
}
