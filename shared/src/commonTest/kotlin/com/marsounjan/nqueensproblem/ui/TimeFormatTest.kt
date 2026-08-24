package com.marsounjan.nqueensproblem.ui

import com.marsounjan.nqueensproblem.util.formatGameTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatTest {

    @Test
    fun formatElapsedSeconds_padsSecondsBelowTen() {
        assertEquals("0:05", formatGameTime(5_000))
    }

    @Test
    fun formatElapsedSeconds_handlesMinutes() {
        assertEquals("1:00", formatGameTime(60_000))
        assertEquals("2:34", formatGameTime(154_000))
    }

    @Test
    fun formatElapsedSeconds_zero() {
        assertEquals("0:00", formatGameTime(0))
    }
}
