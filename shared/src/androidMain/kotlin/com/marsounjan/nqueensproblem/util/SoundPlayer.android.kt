package com.marsounjan.nqueensproblem.util

import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nqueensproblem.shared.generated.resources.Res
import java.io.File

actual fun createSoundPlayer(): SoundPlayer = AndroidSoundPlayer()

/**
 * Each sound is decoded on its first use and kept ready afterwards - nothing is loaded upfront at
 * startup, so app launch never waits on audio decoding.
 */
class AndroidSoundPlayer : SoundPlayer {

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

    private val mutex = Mutex()
    private val soundIds = mutableMapOf<Sound, Int>()

    override suspend fun play(sound: Sound) {
        val soundId = mutex.withLock { soundIds.getOrPut(sound) { loadSound(sound.path) } }
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    private suspend fun loadSound(resourcePath: String): Int {
        val bytes = Res.readBytes(resourcePath)
        val soundId = withContext(Dispatchers.IO) {
            val tempFile = File.createTempFile("sfx", ".mp3").apply { deleteOnExit() }
            tempFile.writeBytes(bytes)
            soundPool.load(tempFile.absolutePath, 1)
        }
        val (_, status) = loadCompletions.first { (id, _) -> id == soundId }
        check(status == 0) { "SoundPool failed to load $resourcePath (status=$status)" }
        return soundId
    }
}
