package com.marsounjan.nqueensproblem.testing

import com.marsounjan.nqueensproblem.sound.SoundPlayer

class FakeSoundPlayer : SoundPlayer {
    var safePlacementCount = 0
        private set
    var conflictPlacementCount = 0
        private set
    var winCount = 0
        private set

    override fun playQueenPlacedSafe() {
        safePlacementCount++
    }

    override fun playQueenPlacedConflict() {
        conflictPlacementCount++
    }

    override fun playWin() {
        winCount++
    }
}
