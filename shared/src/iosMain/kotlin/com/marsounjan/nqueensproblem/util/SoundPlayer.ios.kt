package com.marsounjan.nqueensproblem.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import nqueensproblem.shared.generated.resources.Res
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

actual fun createSoundPlayer(): SoundPlayer = IosSoundPlayer()

/**
 * Each sound is decoded on its first use and kept ready afterwards - nothing is loaded upfront at
 * startup, so app launch never waits on audio decoding.
 */
class IosSoundPlayer : SoundPlayer {

    private val mutex = Mutex()
    private val players = mutableMapOf<Sound, AVAudioPlayer?>()

    override suspend fun play(sound: Sound) {
        val player = mutex.withLock {
            players[sound] ?: loadPlayer(sound.path).also { players[sound] = it }
        }
        withContext(Dispatchers.Main) { player?.playFromStart() }
    }

    private suspend fun loadPlayer(resourcePath: String): AVAudioPlayer? {
        val bytes = Res.readBytes(resourcePath)
        return withContext(Dispatchers.Default) { buildPlayer(bytes) }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun buildPlayer(bytes: ByteArray): AVAudioPlayer? =
        AVAudioPlayer(data = bytes.toNSData(), error = null).also { it.prepareToPlay() }

    private fun AVAudioPlayer.playFromStart() {
        currentTime = 0.0
        play()
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
}
