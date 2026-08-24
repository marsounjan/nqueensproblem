package com.marsounjan.nqueensproblem.util

import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun formatGameTime(millis: Long): String =
    millis.toDuration(DurationUnit.MILLISECONDS).toComponents { minutes, seconds, nanoseconds ->
        val centiseconds = nanoseconds / 10_000_000
        "$minutes:${seconds.toString().padStart(2, '0')}.${centiseconds.toString().padStart(2, '0')}"
    }
