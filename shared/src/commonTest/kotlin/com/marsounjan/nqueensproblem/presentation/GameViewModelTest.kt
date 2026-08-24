package com.marsounjan.nqueensproblem.presentation

import androidx.lifecycle.SavedStateHandle
import com.marsounjan.nqueensproblem.testing.FakeBestTimesRepository
import com.marsounjan.nqueensproblem.testing.FakeNavigator
import com.marsounjan.nqueensproblem.testing.FakeSoundPlayer
import com.marsounjan.nqueensproblem.testing.FakeTimeSource
import com.marsounjan.nqueensproblem.testing.ViewModelTest
import com.marsounjan.nqueensproblem.ui.game.GameBoardPosition
import com.marsounjan.nqueensproblem.ui.game.GameViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

// A known, non-conflicting solution for a 4x4 board.
private val FOUR_QUEENS_SOLUTION = listOf(GameBoardPosition(0, 1), GameBoardPosition(1, 3), GameBoardPosition(2, 0), GameBoardPosition(3, 2))

class GameViewModelTest : ViewModelTest() {

    private fun createViewModel(
        boardSize: Int = 4,
        repository: FakeBestTimesRepository = FakeBestTimesRepository(),
        soundPlayer: FakeSoundPlayer = FakeSoundPlayer(),
        navigator: FakeNavigator = FakeNavigator(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        timeSource: FakeTimeSource = FakeTimeSource(),
    ) = GameViewModel(boardSize, repository, soundPlayer, navigator, savedStateHandle, timeSource)

    @Test
    fun initialState_isAnEmptyBoardWithNoElapsedTime() = runTest(testDispatcher) {
        val viewModel = createViewModel(boardSize = 4)
        val state = viewModel.uiState.value

        assertEquals(4, state.boardState.boardSize)
        assertTrue(state.boardState.queens.isEmpty())
        assertEquals(4, state.boardState.queensRemaining)
        assertEquals(0, state.elapsedMillis)
        assertFalse(state.isTimerRunning)
        assertFalse(state.boardState.isSolved)
        assertNull(state.winDialog)
    }

    @Test
    fun onCell_Tapped_onEmptySquare_placesQueenAndPlaysSafeSound() = runTest(testDispatcher) {
        val soundPlayer = FakeSoundPlayer()
        val viewModel = createViewModel(soundPlayer = soundPlayer)

        viewModel.cellTapped(GameBoardPosition(0, 1))

        assertEquals(setOf(GameBoardPosition(0, 1)), viewModel.uiState.value.boardState.queens)
        assertEquals(1, soundPlayer.safePlacementCount)
        assertEquals(0, soundPlayer.conflictPlacementCount)
    }

    @Test
    fun onCell_Tapped_creatingAConflict_playsConflictSound() = runTest(testDispatcher) {
        val soundPlayer = FakeSoundPlayer()
        val viewModel = createViewModel(soundPlayer = soundPlayer)

        viewModel.cellTapped(GameBoardPosition(0, 0))
        viewModel.cellTapped(GameBoardPosition(0, 1)) // same row as the first queen

        assertTrue(GameBoardPosition(0, 1) in viewModel.uiState.value.boardState.conflictingQueens)
        assertEquals(1, soundPlayer.safePlacementCount)
        assertEquals(1, soundPlayer.conflictPlacementCount)
    }

    @Test
    fun onCell_Tapped_onOccupiedSquare_removesQueen_withoutPlayingASound() = runTest(testDispatcher) {
        val soundPlayer = FakeSoundPlayer()
        val viewModel = createViewModel(soundPlayer = soundPlayer)

        viewModel.cellTapped(GameBoardPosition(0, 0))
        viewModel.cellTapped(GameBoardPosition(0, 0))

        assertTrue(viewModel.uiState.value.boardState.queens.isEmpty())
        assertEquals(1, soundPlayer.safePlacementCount)
        assertEquals(0, soundPlayer.conflictPlacementCount)
    }

    @Test
    fun screenResumed_marksTheTimerAsRunning_withoutChangingElapsedTimeYet() = runTest(testDispatcher) {
        val timeSource = FakeTimeSource()
        val viewModel = createViewModel(timeSource = timeSource)

        viewModel.screenResumed()
        timeSource.advanceBy(3_500.milliseconds)

        // Elapsed time is only materialized on pause/win - the live tick happens in Compose.
        assertTrue(viewModel.uiState.value.isTimerRunning)
        assertEquals(0, viewModel.uiState.value.elapsedMillis)
    }

    @Test
    fun screenResumed_calledAgainWhileAlreadyRunning_doesNotResetTheStartMark() = runTest(testDispatcher) {
        val timeSource = FakeTimeSource()
        val viewModel = createViewModel(timeSource = timeSource)

        viewModel.screenResumed()
        timeSource.advanceBy(2_000.milliseconds)
        viewModel.screenResumed() // e.g. a duplicate resume event; must be a no-op
        timeSource.advanceBy(1_000.milliseconds)
        viewModel.screenPaused()

        assertEquals(3_000, viewModel.uiState.value.elapsedMillis)
    }

    @Test
    fun screenPaused_freezesElapsedTimeAccumulatedSinceResume() = runTest(testDispatcher) {
        val timeSource = FakeTimeSource()
        val viewModel = createViewModel(timeSource = timeSource)

        viewModel.screenResumed()
        timeSource.advanceBy(2_000.milliseconds)
        viewModel.screenPaused()

        assertEquals(2_000, viewModel.uiState.value.elapsedMillis)
        assertFalse(viewModel.uiState.value.isTimerRunning)

        // Time passing while paused must not count.
        timeSource.advanceBy(5_000.milliseconds)
        assertEquals(2_000, viewModel.uiState.value.elapsedMillis)
    }

    @Test
    fun screenPaused_withoutHavingBeenResumed_isNoOp() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.screenPaused()

        assertEquals(0, viewModel.uiState.value.elapsedMillis)
        assertFalse(viewModel.uiState.value.isTimerRunning)
    }

    @Test
    fun multipleResumePausePairs_accumulateElapsedTime() = runTest(testDispatcher) {
        val timeSource = FakeTimeSource()
        val viewModel = createViewModel(timeSource = timeSource)

        viewModel.screenResumed()
        timeSource.advanceBy(2_000.milliseconds)
        viewModel.screenPaused()

        viewModel.screenResumed()
        timeSource.advanceBy(1_500.milliseconds)
        viewModel.screenPaused()

        assertEquals(3_500, viewModel.uiState.value.elapsedMillis)
    }

    @Test
    fun solvingTheBoard_stopsTimer_playsWinSound_andReportsTheExactTime() = runTest(testDispatcher) {
        val repository = FakeBestTimesRepository()
        val soundPlayer = FakeSoundPlayer()
        val timeSource = FakeTimeSource()
        val viewModel = createViewModel(repository = repository, soundPlayer = soundPlayer, timeSource = timeSource)

        viewModel.screenResumed()
        timeSource.advanceBy(4_000.milliseconds)

        FOUR_QUEENS_SOLUTION.forEach { viewModel.cellTapped(it) }

        val state = viewModel.uiState.value
        assertTrue(state.boardState.isSolved)
        assertEquals(4_000, state.elapsedMillis)
        assertFalse(state.isTimerRunning)
        assertEquals(1, soundPlayer.winCount)
        assertEquals(listOf(4 to 4_000L), repository.reportedSolves)
        assertEquals(4_000, state.winDialog?.elapsedMillis)
        assertTrue(state.winDialog?.isNewBest ?: false)

        // Timer no longer runs after solving.
        timeSource.advanceBy(5_000.milliseconds)
        assertEquals(4_000, viewModel.uiState.value.elapsedMillis)
    }

    @Test
    fun solvingSlowerThanExistingBest_isNotFlaggedAsNewBest() = runTest(testDispatcher) {
        val repository = FakeBestTimesRepository().apply { seedBestTime(boardSize = 4, elapsedSeconds = 2) }
        val timeSource = FakeTimeSource()
        val viewModel = createViewModel(repository = repository, timeSource = timeSource)

        viewModel.screenResumed()
        timeSource.advanceBy(4_000.milliseconds)

        FOUR_QUEENS_SOLUTION.forEach { viewModel.cellTapped(it) }

        assertFalse(viewModel.uiState.value.winDialog?.isNewBest ?: true)
        assertEquals(2, viewModel.uiState.value.bestTimeMillis) // unbeaten best stays
    }

    @Test
    fun afterSolved_screenResumed_isIgnored() = runTest(testDispatcher) {
        val timeSource = FakeTimeSource()
        val viewModel = createViewModel(timeSource = timeSource)
        FOUR_QUEENS_SOLUTION.forEach { viewModel.cellTapped(it) }

        viewModel.screenResumed()
        timeSource.advanceBy(9_999.milliseconds)

        assertFalse(viewModel.uiState.value.isTimerRunning)
        assertEquals(0, viewModel.uiState.value.elapsedMillis)
    }

    @Test
    fun onCell_Tapped_afterSolved_isIgnored() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        FOUR_QUEENS_SOLUTION.forEach { viewModel.cellTapped(it) }

        val solvedQueens = viewModel.uiState.value.boardState.queens
        viewModel.cellTapped(GameBoardPosition(0, 0))

        assertEquals(solvedQueens, viewModel.uiState.value.boardState.queens)
    }

    @Test
    fun gameState_survivesRecreationFromTheSameSavedStateHandle() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle()
        val timeSource = FakeTimeSource()
        val firstViewModel = createViewModel(savedStateHandle = savedStateHandle, timeSource = timeSource)
        firstViewModel.screenResumed()
        timeSource.advanceBy(6_000.milliseconds)
        firstViewModel.cellTapped(GameBoardPosition(0, 1))
        firstViewModel.screenPaused()

        val recreatedViewModel = createViewModel(savedStateHandle = savedStateHandle)
        val state = recreatedViewModel.uiState.value

        assertEquals(setOf(GameBoardPosition(0, 1)), state.boardState.queens)
        assertEquals(6_000, state.elapsedMillis)
    }

    @Test
    fun winDialogConfirmClicked_navigatesBack() = runTest(testDispatcher) {
        val navigator = FakeNavigator()
        val viewModel = createViewModel(navigator = navigator)

        viewModel.winDialogConfirmClicked()

        assertEquals(1, navigator.goBackCallCount)
    }
}
