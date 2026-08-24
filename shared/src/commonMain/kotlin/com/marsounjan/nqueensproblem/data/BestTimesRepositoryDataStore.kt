package com.marsounjan.nqueensproblem.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

private const val PREFERENCES_FILE_NAME = "nqueens.preferences_pb"

internal fun createBestTimesDataStore(writableDirectory: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { "$writableDirectory/$PREFERENCES_FILE_NAME".toPath() },
    )

fun createBestTimesRepository(writableDirectory: String): BestTimesRepository =
    BestTimesRepositoryDataStore(createBestTimesDataStore(writableDirectory))

class BestTimesRepositoryDataStore(
    private val dataStore: DataStore<Preferences>,
) : BestTimesRepository {

    override fun bestTimeMillis(boardSize: Int): Flow<Long?> =
        dataStore.data.map { preferences -> preferences[keyFor(boardSize)] }

    override suspend fun storeBestTime(boardSize: Int, millis: Long) {
        dataStore.edit { preferences ->
            val currentBest = preferences[keyFor(boardSize)]
            if (currentBest == null || millis < currentBest) {
                preferences[keyFor(boardSize)] = millis
            }
        }
    }

    private fun keyFor(boardSize: Int) = longPreferencesKey("best_time_seconds_$boardSize")
}
