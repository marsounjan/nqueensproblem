package com.marsounjan.nqueensproblem.util

interface SoundPlayer {
    suspend fun play(sound: Sound)
}

enum class Sound(val path: String) {
    QUEEN_PLACED_SAFE("files/queen_placed_safe.mp3"),
    QUEEN_PLACED_CONFLICT("files/queen_placed_conflict.mp3"),
    WIN("files/game_won.mp3"),
}

expect fun createSoundPlayer(): SoundPlayer
