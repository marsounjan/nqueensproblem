package com.marsounjan.nqueensproblem.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BestTimesRepositoryDataStoreTest {

    private lateinit var directory: File
    private lateinit var repository: BestTimesRepository

    @BeforeTest
    fun setUp() {
        directory = File.createTempFile("best-times-test", "").apply {
            delete()
            mkdirs()
        }
        // Constructed directly (not via the memoized createBestTimesRepository singleton) so
        // each test gets its own isolated DataStore instance/file.
        repository = BestTimesRepositoryDataStore(createBestTimesDataStore(directory.absolutePath))
    }

    @AfterTest
    fun tearDown() {
        directory.deleteRecursively()
    }

    @Test
    fun bestTimeSeconds_withNoRecordedTime_isNull() = runTest {
        assertNull(repository.bestTimeMillis(boardSize = 8).first())
    }

    @Test
    fun storeBestTime_firstResult_becomesTheBest() = runTest {
        repository.storeBestTime(boardSize = 8, millis = 42)
        assertEquals(42, repository.bestTimeMillis(boardSize = 8).first())
    }

    @Test
    fun storeBestTime_slowerThanCurrentBest_isIgnored() = runTest {
        repository.storeBestTime(boardSize = 8, millis = 20)
        repository.storeBestTime(boardSize = 8, millis = 30)
        assertEquals(20, repository.bestTimeMillis(boardSize = 8).first())
    }

    @Test
    fun storeBestTime_fasterThanCurrentBest_replacesIt() = runTest {
        repository.storeBestTime(boardSize = 8, millis = 30)
        repository.storeBestTime(boardSize = 8, millis = 20)
        assertEquals(20, repository.bestTimeMillis(boardSize = 8).first())
    }

    @Test
    fun bestTimes_areTrackedIndependentlyPerBoardSize() = runTest {
        repository.storeBestTime(boardSize = 6, millis = 15)
        repository.storeBestTime(boardSize = 10, millis = 99)

        assertEquals(15, repository.bestTimeMillis(boardSize = 6).first())
        assertEquals(99, repository.bestTimeMillis(boardSize = 10).first())
        assertNull(repository.bestTimeMillis(boardSize = 7).first())
    }
}
