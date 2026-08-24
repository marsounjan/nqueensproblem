package com.marsounjan.nqueensproblem.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marsounjan.nqueensproblem.data.BestTimesRepository
import com.marsounjan.nqueensproblem.ui.navigation.Navigator
import com.marsounjan.nqueensproblem.util.Sound
import com.marsounjan.nqueensproblem.util.SoundPlayer
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
    val boardState: GameBoardState,
    val elapsedMillis: Long,
    val bestTimeMillis: Long?,
    val winDialog: WinDialogModel?,
)

class GameViewModel(
    private val boardSize: Int,
    private val bestTimesRepository: BestTimesRepository,
    private val soundPlayer: SoundPlayer,
    private val navigator: Navigator,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val boardFlow = MutableStateFlow(
        GameBoardState(
            boardSize = this@GameViewModel.boardSize, queens = decodeQueens(
                savedStateHandle[KEY_QUEENS],
                this@GameViewModel.boardSize
            )
        ),
    )
    private val elapsedMillisFlow =
        MutableStateFlow(savedStateHandle.get<Long>(KEY_ELAPSED_SECONDS) ?: 0L)
    private val isNewBestFlow = MutableStateFlow(false)

    private var tickerJob: Job? = null

    val uiState: StateFlow<GameUiState> = combine(
        boardFlow,
        elapsedMillisFlow,
        bestTimesRepository.bestTimeMillis(this@GameViewModel.boardSize),
        isNewBestFlow,
    ) { board, elapsedSeconds, bestTimeSeconds, isNewBest ->
        board.toUiState(elapsedSeconds, bestTimeSeconds, isNewBest)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = boardFlow.value.toUiState(
            elapsedMillis = elapsedMillisFlow.value,
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
                elapsedMillisFlow.value += ELAPSED_TIME_UPDATE_PERIOD_MILLIS
                savedStateHandle[KEY_ELAPSED_SECONDS] = elapsedMillisFlow.value
            }
        }
    }

    /** Called when the game screen is backgrounded/no longer resumed. */
    fun screenPaused() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun cellTapped(position: GameBoardPosition) {
        val previousBoard = boardFlow.value
        if (previousBoard.isSolved) return

        val wasPlacing = position !in previousBoard.queens
        val newBoard = previousBoard.toggle(position)
        if (newBoard == previousBoard) return

        boardFlow.value = newBoard
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
        screenPaused()
        viewModelScope.launch { soundPlayer.play(Sound.WIN) }
        viewModelScope.launch {
            val previousBest =
                bestTimesRepository.bestTimeMillis(this@GameViewModel.boardSize).first()
            val finishTime = elapsedMillisFlow.value
            bestTimesRepository.storeBestTime(this@GameViewModel.boardSize, finishTime)
            isNewBestFlow.value = previousBest == null || finishTime < previousBest
        }
    }

    private fun GameBoardState.toUiState(
        elapsedMillis: Long,
        bestTimeSeconds: Long?,
        isNewBest: Boolean
    ) = GameUiState(
        boardState = this,
        elapsedMillis = elapsedMillis,
        bestTimeMillis = bestTimeSeconds,
        winDialog = if (isSolved) {
            WinDialogModel(
                elapsedMillis = elapsedMillis,
                isNewBest = isNewBest
            )
        } else null,
    )

    private companion object {
        const val KEY_QUEENS = "queens"
        const val KEY_ELAPSED_SECONDS = "elapsed_seconds"
        private const val ELAPSED_TIME_UPDATE_PERIOD_MILLIS: Long = 100
    }
}

private fun encodeQueens(queens: Set<GameBoardPosition>, boardSize: Int): IntArray =
    queens.map { it.row * boardSize + it.col }.toIntArray()

private fun decodeQueens(encoded: IntArray?, boardSize: Int): Set<GameBoardPosition> =
    encoded?.map { GameBoardPosition(row = it / boardSize, col = it % boardSize) }?.toSet()
        ?: emptySet()
