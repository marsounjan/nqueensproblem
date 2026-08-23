package com.marsounjan.nqueensproblem.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsounjan.nqueensproblem.data.BestTimesRepository
import com.marsounjan.nqueensproblem.domain.Board
import com.marsounjan.nqueensproblem.domain.Position
import com.marsounjan.nqueensproblem.ui.navigation.Navigator
import com.marsounjan.nqueensproblem.sound.SoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class GameUiState(
    val boardSize: Int,
    val queens: Set<Position>,
    val conflictingQueens: Set<Position>,
    val attackedSquares: Set<Position>,
    val queensRemaining: Int,
    val elapsedSeconds: Long,
    val bestTimeSeconds: Long?,
    val isSolved: Boolean,
    val isNewBest: Boolean,
)

class GameViewModel(
    private val boardSize: Int,
    private val bestTimesRepository: BestTimesRepository,
    private val soundPlayer: SoundPlayer,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val boardFlow = MutableStateFlow(
        Board(size = boardSize, queens = decodeQueens(savedStateHandle[KEY_QUEENS], boardSize)),
    )
    private val elapsedSecondsFlow = MutableStateFlow(savedStateHandle.get<Long>(KEY_ELAPSED_SECONDS) ?: 0L)
    private val isNewBestFlow = MutableStateFlow(false)

    private var tickerJob: Job? = null

    val uiState: StateFlow<GameUiState> = combine(
        boardFlow,
        elapsedSecondsFlow,
        bestTimesRepository.bestTimeMillis(boardSize),
        isNewBestFlow,
    ) { board, elapsedSeconds, bestTimeSeconds, isNewBest ->
        board.toUiState(elapsedSeconds, bestTimeSeconds, isNewBest)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = boardFlow.value.toUiState(
            elapsedSeconds = elapsedSecondsFlow.value,
            bestTimeSeconds = null,
            isNewBest = false,
        ),
    )

    /** Called when the game screen becomes (or stays) foregrounded/resumed. */
    fun screenResumed() {
        if (tickerJob != null || boardFlow.value.isSolved) return
        tickerJob = viewModelScope.launch {
            while (isActive) {
                delay(ELAPSED_TIME_UPDATE_PERIOD_MILLIS.milliseconds)
                elapsedSecondsFlow.value += ELAPSED_TIME_UPDATE_PERIOD_MILLIS
                savedStateHandle[KEY_ELAPSED_SECONDS] = elapsedSecondsFlow.value
            }
        }
    }

    /** Called when the game screen is backgrounded/no longer resumed. */
    fun screenPaused() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun tapCell(position: Position) {
        val previousBoard = boardFlow.value
        if (previousBoard.isSolved) return

        val wasPlacing = position !in previousBoard.queens
        val newBoard = previousBoard.toggle(position)
        if (newBoard == previousBoard) return

        boardFlow.value = newBoard
        savedStateHandle[KEY_QUEENS] = encodeQueens(newBoard.queens, boardSize)

        if (wasPlacing) {
            if (position in newBoard.conflictingQueens()) {
                soundPlayer.playQueenPlacedConflict()
            } else {
                soundPlayer.playQueenPlacedSafe()
            }
        }

        if (newBoard.isSolved) onSolved(newBoard)
    }

    fun reset() {
        screenPaused()
        boardFlow.value = Board(size = boardSize)
        elapsedSecondsFlow.value = 0
        isNewBestFlow.value = false
        savedStateHandle[KEY_QUEENS] = IntArray(0)
        savedStateHandle[KEY_ELAPSED_SECONDS] = 0L
    }

    // Navigator is passed per-call rather than stored as a field: this ViewModel outlives
    // rotation (its ViewModelStore survives config changes), but the Navigator wraps a Nav3
    // back stack that gets a new object identity after rotation - a stored reference would go
    // stale and silently mutate an orphaned back stack that NavDisplay no longer observes.
    fun onChangeSizeClicked(navigator: Navigator) {
        navigator.goBack()
    }

    private fun onSolved(solvedBoard: Board) {
        screenPaused()
        soundPlayer.playWin()
        viewModelScope.launch {
            val previousBest = bestTimesRepository.bestTimeMillis(boardSize).first()
            val finishTime = elapsedSecondsFlow.value
            bestTimesRepository.storeBestTime(boardSize, finishTime)
            isNewBestFlow.value = previousBest == null || finishTime < previousBest
        }
    }

    private fun Board.toUiState(elapsedSeconds: Long, bestTimeSeconds: Long?, isNewBest: Boolean) = GameUiState(
        boardSize = size,
        queens = queens,
        conflictingQueens = conflictingQueens(),
        attackedSquares = attackedSquares(),
        queensRemaining = queensRemaining,
        elapsedSeconds = elapsedSeconds,
        bestTimeSeconds = bestTimeSeconds,
        isSolved = isSolved,
        isNewBest = isNewBest,
    )

    private companion object {
        const val KEY_QUEENS = "queens"
        const val KEY_ELAPSED_SECONDS = "elapsed_seconds"
        private const val ELAPSED_TIME_UPDATE_PERIOD_MILLIS : Long = 100
    }
}

private fun encodeQueens(queens: Set<Position>, boardSize: Int): IntArray =
    queens.map { it.row * boardSize + it.col }.toIntArray()

private fun decodeQueens(encoded: IntArray?, boardSize: Int): Set<Position> =
    encoded?.map { Position(row = it / boardSize, col = it % boardSize) }?.toSet() ?: emptySet()
