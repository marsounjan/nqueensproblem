package com.marsounjan.nqueensproblem.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsounjan.nqueensproblem.data.BestTimesRepository
import com.marsounjan.nqueensproblem.ui.navigation.Navigator
import com.marsounjan.nqueensproblem.util.Sound
import com.marsounjan.nqueensproblem.util.SoundPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.TimeMark
import kotlin.time.TimeSource

data class GameUiState(
    val boardState: GameBoardState,
    val elapsedMillis: Long,
    val isTimerRunning: Boolean,
    val bestTimeMillis: Long?,
    val winDialog: WinDialogModel?,
)

class GameViewModel(
    private val boardSize: Int,
    private val bestTimesRepository: BestTimesRepository,
    private val soundPlayer: SoundPlayer,
    private val navigator: Navigator,
    private val savedStateHandle: SavedStateHandle,
    private val timeSource: TimeSource = TimeSource.Monotonic,
) : ViewModel() {

    private val boardState = MutableStateFlow(
        GameBoardState(
            boardSize = this@GameViewModel.boardSize, queens = decodeQueens(
                savedStateHandle[KEY_QUEENS],
                this@GameViewModel.boardSize
            )
        ),
    )

    // Elapsed time accumulated up to the last time the ticker was stopped (paused or won).
    private val elapsedMillis =
        MutableStateFlow(savedStateHandle.get<Long>(KEY_ELAPSED_MILLIS) ?: 0L)
    private val isTimeRunning = MutableStateFlow(false)
    private val isNewBest = MutableStateFlow(false)

    // Set while the ticker is running; used to compute the exact elapsed time on demand.
    private var tickerStartMark: TimeMark? = null

    private val bestTime: Flow<Long?> =
        bestTimesRepository.bestTimeMillis(this@GameViewModel.boardSize)

    val uiState: StateFlow<GameUiState> = combine(
        boardState,
        elapsedMillis,
        isTimeRunning,
        bestTime,
        isNewBest,
    ) { board, elapsedMillis, isTimerRunning, bestTimeMillis, isNewBest ->
        GameUiState(
            boardState = board,
            elapsedMillis = elapsedMillis,
            isTimerRunning = isTimerRunning,
            bestTimeMillis = bestTimeMillis,
            winDialog = if (board.isSolved) {
                WinDialogModel(
                    elapsedMillis = elapsedMillis,
                    isNewBest = isNewBest
                )
            } else null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue =
            GameUiState(
                boardState = boardState.value,
                elapsedMillis = elapsedMillis.value,
                isTimerRunning = false,
                bestTimeMillis = null,
                winDialog = null,
            )
    )

    /** Called when the game screen becomes (or stays) foregrounded/resumed. */
    fun screenResumed() {
        if (tickerStartMark != null || boardState.value.isSolved) return
        tickerStartMark = timeSource.markNow()
        isTimeRunning.value = true
    }

    /** Called when the game screen is backgrounded/no longer resumed. */
    fun screenPaused() {
        val startMark = tickerStartMark ?: return
        elapsedMillis.value += startMark.elapsedNow().inWholeMilliseconds
        tickerStartMark = null
        isTimeRunning.value = false
        savedStateHandle[KEY_ELAPSED_MILLIS] = elapsedMillis.value
    }

    fun cellTapped(position: GameBoardPosition) {
        val previousBoard = boardState.value
        if (previousBoard.isSolved) return

        val wasPlacing = position !in previousBoard.queens
        val newBoard = previousBoard.toggle(position)
        if (newBoard == previousBoard) return

        boardState.value = newBoard
        savedStateHandle[KEY_QUEENS] = encodeQueens(newBoard.queens, this@GameViewModel.boardSize)

        if (wasPlacing) {
            val sound = if (position in newBoard.conflictingQueens) {
                Sound.QUEEN_PLACED_CONFLICT
            } else {
                Sound.QUEEN_PLACED_SAFE
            }
            viewModelScope.launch { soundPlayer.play(sound) }
        }

        if (newBoard.isSolved) onSolved()
    }

    fun winDialogConfirmClicked() {
        navigator.goBack()
    }

    private fun onSolved() {
        screenPaused() // freezes and persists the exact elapsed time at the moment of winning
        val finishTime = elapsedMillis.value
        viewModelScope.launch { soundPlayer.play(Sound.WIN) }
        viewModelScope.launch {
            val previousBest =
                bestTimesRepository.bestTimeMillis(this@GameViewModel.boardSize).first()
            bestTimesRepository.storeBestTime(this@GameViewModel.boardSize, finishTime)
            isNewBest.value = previousBest == null || finishTime < previousBest
        }
    }

    private companion object {
        const val KEY_QUEENS = "queens"
        const val KEY_ELAPSED_MILLIS = "elapsed_millis"
    }
}

private fun encodeQueens(queens: Set<GameBoardPosition>, boardSize: Int): IntArray =
    queens.map { it.row * boardSize + it.col }.toIntArray()

private fun decodeQueens(encoded: IntArray?, boardSize: Int): Set<GameBoardPosition> =
    encoded?.map { GameBoardPosition(row = it / boardSize, col = it % boardSize) }?.toSet()
        ?: emptySet()
