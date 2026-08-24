package com.marsounjan.nqueensproblem.ui.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsounjan.nqueensproblem.AppConfig
import com.marsounjan.nqueensproblem.data.BestTimesRepository
import com.marsounjan.nqueensproblem.ui.navigation.NavigationRoute
import com.marsounjan.nqueensproblem.ui.navigation.Navigator
import com.marsounjan.nqueensproblem.util.WhileSubscribed5s
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@Immutable
data class HomeScreenUiState(
    val boardSize: Int,
    val bestTimeSeconds: Long?,
    val minBoardSize: Int = AppConfig.MIN_BOARD_SIZE,
    val maxBoardSize: Int = AppConfig.MAX_BOARD_SIZE,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModel(
    bestTimesRepository: BestTimesRepository,
    private val navigator: Navigator,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val boardSize: StateFlow<Int> =
        savedStateHandle.getStateFlow(KEY_BOARD_SIZE, AppConfig.DEFAULT_BOARD_SIZE)

    // distinctUntilChanged avoids re-emitting when switching board size lands on the same
    // best time value (e.g. two sizes both without a record yet).
    private val bestTimeMillis: Flow<Long?> =
        boardSize
            .flatMapLatest { size -> bestTimesRepository.bestTimeMillis(size) }
            .distinctUntilChanged()

    val uiState: StateFlow<HomeScreenUiState> =
        combine(
            boardSize,
            bestTimeMillis,
        ) { size, bestTime ->
            HomeScreenUiState(
                boardSize = size,
                bestTimeSeconds = bestTime
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed5s,
            initialValue = HomeScreenUiState(
                boardSize = boardSize.value,
                bestTimeSeconds = null
            ),
        )

    fun onBoardSizeChanged(newSize: Int) {
        savedStateHandle[KEY_BOARD_SIZE] =
            newSize.coerceIn(AppConfig.MIN_BOARD_SIZE, AppConfig.MAX_BOARD_SIZE)
    }

    fun onStartClicked() {
        navigator.open(NavigationRoute.GameBoard(uiState.value.boardSize))
    }

    private companion object {
        const val KEY_BOARD_SIZE = "board_size"
    }
}
