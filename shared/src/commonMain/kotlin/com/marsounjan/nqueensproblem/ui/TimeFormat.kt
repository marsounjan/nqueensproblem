package com.marsounjan.nqueensproblem.ui

fun formatElapsedSeconds(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val secondsText = if (seconds < 10) "0$seconds" else "$seconds"
    return "$minutes:$secondsText"
}
