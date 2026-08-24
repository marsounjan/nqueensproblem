package com.marsounjan.nqueensproblem

import com.marsounjan.nqueensproblem.data.BestTimesRepository
import com.marsounjan.nqueensproblem.data.createBestTimesRepository
import com.marsounjan.nqueensproblem.util.SoundPlayer
import com.marsounjan.nqueensproblem.util.createSoundPlayer

/**
 * Simple service locator for platform-agnostic singletons. Each platform entry point
 * (Android [android.app.Application], iOS `iOSApp.init`) must call [init] before any
 * screen is shown, passing a directory the platform guarantees is writable.
 */
object AppServices {
    lateinit var bestTimesRepository: BestTimesRepository
        private set
    lateinit var soundPlayer: SoundPlayer
        private set

    fun init(writableDirectory: String) {
        bestTimesRepository = createBestTimesRepository(writableDirectory)
        soundPlayer = createSoundPlayer()
    }
}
