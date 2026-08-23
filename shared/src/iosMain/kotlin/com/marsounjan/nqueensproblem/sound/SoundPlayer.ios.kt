package com.marsounjan.nqueensproblem.sound

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val safePlayer = lazyPlayer("files/queen_placed_safe.mp3")
    private val conflictPlayer = lazyPlayer("files/queen_placed_conflict.mp3")
    private val winPlayer = lazyPlayer("files/game_won.mp3")

    override fun playQueenPlacedSafe() = play(safePlayer)
    override fun playQueenPlacedConflict() = play(conflictPlayer)
    override fun playWin() = play(winPlayer)

    private fun lazyPlayer(resourcePath: String): Deferred<AVAudioPlayer?> =
        scope.async(start = CoroutineStart.LAZY) { buildPlayer(Res.readBytes(resourcePath)) }

    private fun play(playerDeferred: Deferred<AVAudioPlayer?>) {
        scope.launch {
            val player = playerDeferred.await()
            withContext(Dispatchers.Main) { player?.playFromStart() }
        }
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
