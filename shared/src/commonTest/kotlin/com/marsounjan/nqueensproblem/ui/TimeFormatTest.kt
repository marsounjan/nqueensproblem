package com.marsounjan.nqueensproblem.ui

import com.marsounjan.nqueensproblem.util.formatGameTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatTest {

    @Test
    fun formatElapsedSeconds_padsSecondsBelowTen() {
        assertEquals("0:05.00", formatGameTime(5_000))
    }

    @Test
    fun formatElapsedSeconds_handlesMinutes() {
        assertEquals("1:00.00", formatGameTime(60_000))
        assertEquals("2:34.00", formatGameTime(154_000))
    }

    @Test
    fun formatElapsedSeconds_zero() {
        assertEquals("0:00.00", formatGameTime(0))
    }

    @Test
    fun formatElapsedSeconds_padsCentisecondsBelowTen() {
        assertEquals("0:05.03", formatGameTime(5_030))
    }

    @Test
    fun formatElapsedSeconds_handlesCentiseconds() {
        assertEquals("0:01.23", formatGameTime(1_234))
    }
}
