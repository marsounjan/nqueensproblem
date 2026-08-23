package com.marsounjan.nqueensproblem.sound

import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nqueensproblem.shared.generated.resources.Res
import java.io.File

actual fun createSoundPlayer(): SoundPlayer = AndroidSoundPlayer()

/**
 * Each sound is decoded on its first use and kept ready afterwards - nothing is loaded upfront at
 * startup, so app launch never waits on audio decoding.
 */
class AndroidSoundPlayer : SoundPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    // (sampleId, status) pairs, as reported by SoundPool once a load finishes decoding.
    private val loadCompletions = MutableSharedFlow<Pair<Int, Int>>(extraBufferCapacity = 8)

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            loadCompletions.tryEmit(sampleId to status)
        }
    }

    private val safeSoundId = lazySound("files/queen_placed_safe.mp3")
    private val conflictSoundId = lazySound("files/queen_placed_conflict.mp3")
    private val winSoundId = lazySound("files/game_won.mp3")

    override fun playQueenPlacedSafe() = play(safeSoundId)
    override fun playQueenPlacedConflict() = play(conflictSoundId)
    override fun playWin() = play(winSoundId)

    private fun lazySound(resourcePath: String): Deferred<Int> = scope.async(start = CoroutineStart.LAZY) {
        val tempFile = File.createTempFile("sfx", ".mp3").apply { deleteOnExit() }
        tempFile.writeBytes(Res.readBytes(resourcePath))
        val soundId = soundPool.load(tempFile.absolutePath, 1)
        val (_, status) = loadCompletions.first { (id, _) -> id == soundId }
        check(status == 0) { "SoundPool failed to load $resourcePath (status=$status)" }
        soundId
    }

    private fun play(soundId: Deferred<Int>) {
        scope.launch {
            soundPool.play(soundId.await(), 1f, 1f, 1, 0, 1f)
        }
    }
}
