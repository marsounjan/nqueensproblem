package com.marsounjan.nqueensproblem.testing

import com.marsounjan.nqueensproblem.util.Sound
import com.marsounjan.nqueensproblem.util.SoundPlayer

class FakeSoundPlayer : SoundPlayer {
    private val counts = mutableMapOf<Sound, Int>()

    val safePlacementCount get() = counts[Sound.QUEEN_PLACED_SAFE] ?: 0
    val conflictPlacementCount get() = counts[Sound.QUEEN_PLACED_CONFLICT] ?: 0
    val winCount get() = counts[Sound.WIN] ?: 0

    override suspend fun play(sound: Sound) {
        counts[sound] = (counts[sound] ?: 0) + 1
    }
}
