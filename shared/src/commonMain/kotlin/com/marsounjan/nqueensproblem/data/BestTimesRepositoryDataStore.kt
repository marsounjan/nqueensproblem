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

/**
 * Each platform entry point only needs to supply a writable directory; the DataStore itself
 * (and its file name) is assembled once, here, in common code.
 */
internal fun createBestTimesDataStore(writableDirectory: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        produceFile = { "$writableDirectory/$PREFERENCES_FILE_NAME".toPath() },
    )

/**
 * Platform entry points depend only on [BestTimesRepository] - the DataStore/Preferences types
 * stay an implementation detail of `shared`, so `androidApp`/`iosApp` don't need that dependency
 * on their own classpath.
 *
 * Memoized as a process-wide singleton: DataStore requires exactly one instance per file per
 * process, but platform entry points (e.g. an Android Activity) can be recreated - without this,
 * a second call while the first instance's scope hasn't finished tearing down throws
 * "There are multiple DataStores active for the same file".
 */
private var cachedRepository: BestTimesRepository? = null

fun createBestTimesRepository(writableDirectory: String): BestTimesRepository =
    cachedRepository ?: BestTimesRepositoryDataStore(createBestTimesDataStore(writableDirectory))
        .also { cachedRepository = it }

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
