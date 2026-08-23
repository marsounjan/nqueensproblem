package com.marsounjan.nqueensproblem.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsounjan.nqueensproblem.data.BestTimesRepository
import com.marsounjan.nqueensproblem.AppConfig
import com.marsounjan.nqueensproblem.ui.navigation.NavigationRoute
import com.marsounjan.nqueensproblem.ui.navigation.Navigator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class HomeScreenUiState(
    val boardSize: Int,
    val bestTimeSeconds: Long?,
    val minBoardSize: Int = AppConfig.MIN_BOARD_SIZE,
    val maxBoardSize: Int = AppConfig.MAX_BOARD_SIZE,
)

class HomeScreenViewModel(
    bestTimesRepository: BestTimesRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val boardSizeFlow: StateFlow<Int> =
        savedStateHandle.getStateFlow(KEY_BOARD_SIZE, AppConfig.DEFAULT_BOARD_SIZE)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeScreenUiState> = combine(
        boardSizeFlow,
        boardSizeFlow.flatMapLatest { size -> bestTimesRepository.bestTimeMillis(size) },
    ) { size, bestTime ->
        HomeScreenUiState(boardSize = size, bestTimeSeconds = bestTime)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeScreenUiState(boardSizeFlow.value, bestTimeSeconds = null),
    )

    fun onBoardSizeChanged(newSize: Int) {
        savedStateHandle[KEY_BOARD_SIZE] = newSize.coerceIn(AppConfig.MIN_BOARD_SIZE, AppConfig.MAX_BOARD_SIZE)
    }

    // Navigator is passed per-call rather than stored as a field: this ViewModel outlives
    // rotation (its ViewModelStore survives config changes), but the Navigator wraps a Nav3
    // back stack that gets a new object identity after rotation - a stored reference would go
    // stale and silently mutate an orphaned back stack that NavDisplay no longer observes.
    fun onStartClicked(navigator: Navigator) {
        navigator.open(NavigationRoute.GameBoard(uiState.value.boardSize))
    }

    private companion object {
        const val KEY_BOARD_SIZE = "board_size"
    }
}
