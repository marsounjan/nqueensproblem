package com.marsounjan.nqueensproblem.testing

import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A manually-advanced [TimeSource] so ticking behavior (which reads real elapsed time via
 * [TimeMark.elapsedNow]) can be tested deterministically, without relying on the test coroutine
 * dispatcher's virtual time - the two clocks are unrelated.
 */
class FakeTimeSource : TimeSource {

    private var now = Duration.ZERO

    fun advanceBy(duration: Duration) {
        now += duration
    }

    override fun markNow(): TimeMark {
        val markedAt = now
        return object : TimeMark {
            override fun elapsedNow(): Duration = now - markedAt
        }
    }
}
