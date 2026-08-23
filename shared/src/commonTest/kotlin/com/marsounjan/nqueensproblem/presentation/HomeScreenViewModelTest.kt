package com.marsounjan.nqueensproblem.presentation

import androidx.lifecycle.SavedStateHandle
import com.marsounjan.nqueensproblem.AppConfig
import com.marsounjan.nqueensproblem.testing.FakeBestTimesRepository
import com.marsounjan.nqueensproblem.testing.FakeNavigator
import com.marsounjan.nqueensproblem.testing.ViewModelTest
import com.marsounjan.nqueensproblem.ui.home.HomeScreenViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HomeScreenViewModelTest : ViewModelTest() {

    private fun createViewModel(
        repository: FakeBestTimesRepository = FakeBestTimesRepository(),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ) = HomeScreenViewModel(repository, savedStateHandle)

    @Test
    fun initialState_defaultsToMinBoardSize_withNoBestTime() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals(AppConfig.MIN_BOARD_SIZE, state.boardSize)
        assertNull(state.bestTimeSeconds)
    }

    @Test
    fun onBoardSizeChanged_updatesStateAndTracksItsOwnBestTime() = runTest(testDispatcher) {
        val repository = FakeBestTimesRepository().apply {
            seedBestTime(boardSize = 4, elapsedSeconds = 30)
            seedBestTime(boardSize = 6, elapsedSeconds = 12)
        }
        val viewModel = createViewModel(repository = repository)

        assertEquals(30, viewModel.uiState.value.bestTimeSeconds)

        viewModel.onBoardSizeChanged(6)

        assertEquals(6, viewModel.uiState.value.boardSize)
        assertEquals(12, viewModel.uiState.value.bestTimeSeconds)
    }

    @Test
    fun onBoardSizeChanged_clampsToConfiguredRange() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onBoardSizeChanged(AppConfig.MAX_BOARD_SIZE + 5)
        assertEquals(AppConfig.MAX_BOARD_SIZE, viewModel.uiState.value.boardSize)

        viewModel.onBoardSizeChanged(AppConfig.MIN_BOARD_SIZE - 5)
        assertEquals(AppConfig.MIN_BOARD_SIZE, viewModel.uiState.value.boardSize)
    }

    @Test
    fun boardSizeSelection_survivesRecreationFromTheSameSavedStateHandle() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle()
        val firstViewModel = createViewModel(savedStateHandle = savedStateHandle)
        firstViewModel.onBoardSizeChanged(8)

        val recreatedViewModel = createViewModel(savedStateHandle = savedStateHandle)
        assertEquals(8, recreatedViewModel.uiState.value.boardSize)
    }

    @Test
    fun onStartClicked_navigatesToGame_withCurrentlySelectedBoardSize() = runTest(testDispatcher) {
        val navigator = FakeNavigator()
        val viewModel = createViewModel()
        viewModel.onBoardSizeChanged(6)

        viewModel.onStartClicked(navigator)

        assertEquals(listOf(6), navigator.openGameCalls)
    }
}
