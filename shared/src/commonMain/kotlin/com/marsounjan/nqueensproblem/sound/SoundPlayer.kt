package com.marsounjan.nqueensproblem.sound

interface SoundPlayer {
    fun playQueenPlacedSafe()
    fun playQueenPlacedConflict()
    fun playWin()
}

expect fun createSoundPlayer(): SoundPlayer
