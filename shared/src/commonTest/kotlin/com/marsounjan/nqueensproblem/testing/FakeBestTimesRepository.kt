package com.marsounjan.nqueensproblem.testing

import com.marsounjan.nqueensproblem.data.BestTimesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeBestTimesRepository : BestTimesRepository {

    private val bestTimes = mutableMapOf<Int, MutableStateFlow<Long?>>()
    val reportedSolves = mutableListOf<Pair<Int, Long>>()

    override fun bestTimeMillis(boardSize: Int): StateFlow<Long?> =
        flowFor(boardSize).asStateFlow()

    override suspend fun storeBestTime(boardSize: Int, millis: Long) {
        reportedSolves += boardSize to millis
        val flow = flowFor(boardSize)
        val current = flow.value
        if (current == null || millis < current) {
            flow.value = millis
        }
    }

    fun seedBestTime(boardSize: Int, elapsedSeconds: Long) {
        flowFor(boardSize).value = elapsedSeconds
    }

    private fun flowFor(boardSize: Int) = bestTimes.getOrPut(boardSize) { MutableStateFlow(null) }
}
