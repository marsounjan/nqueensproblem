package com.marsounjan.nqueensproblem

import com.marsounjan.nqueensproblem.data.BestTimesRepository
import com.marsounjan.nqueensproblem.data.createBestTimesRepository
import com.marsounjan.nqueensproblem.util.SoundPlayer
import com.marsounjan.nqueensproblem.util.createSoundPlayer

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
