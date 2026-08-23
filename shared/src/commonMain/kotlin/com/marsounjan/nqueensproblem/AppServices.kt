package com.marsounjan.nqueensproblem

import com.marsounjan.nqueensproblem.data.BestTimesRepository
import com.marsounjan.nqueensproblem.data.createBestTimesRepository
import com.marsounjan.nqueensproblem.sound.SoundPlayer
import com.marsounjan.nqueensproblem.sound.createSoundPlayer

/**
 * Plain process-wide singletons with no Compose lifecycle of their own - unlike [Navigator][com.marsounjan.nqueensproblem.ui.navigation.Navigator],
 * which is tied to the Nav3 back stack and lives behind its own `CompositionLocal`, these don't
 * need to be threaded through the composition at all.
 */
object AppServices {
    lateinit var bestTimesRepository: BestTimesRepository
        private set
    lateinit var soundPlayer: SoundPlayer
        private set

    private var initialized = false

    fun init(writableDirectory: String) {
        if (initialized) return
        bestTimesRepository = createBestTimesRepository(writableDirectory)
        soundPlayer = createSoundPlayer()
        initialized = true
    }
}
