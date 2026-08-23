package com.marsounjan.nqueensproblem.data

import kotlinx.coroutines.flow.Flow

/**
 * Tracks the single fastest solve time per board size - not a history, just the record.
 */
interface BestTimesRepository {
    fun bestTimeMillis(boardSize: Int): Flow<Long?>
    suspend fun storeBestTime(boardSize: Int, millis: Long)
}
