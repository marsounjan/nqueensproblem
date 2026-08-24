package com.marsounjan.nqueensproblem.util

import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun formatGameTime(millis: Long): String =
    millis.toDuration(DurationUnit.MILLISECONDS).toComponents { minutes, seconds, _ ->
        "$minutes:${seconds.toString().padStart(2, '0')}"
    }
