package com.marsounjan.nqueensproblem.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatTest {

    @Test
    fun formatElapsedSeconds_padsSecondsBelowTen() {
        assertEquals("0:05", formatElapsedSeconds(5))
    }

    @Test
    fun formatElapsedSeconds_handlesMinutes() {
        assertEquals("1:00", formatElapsedSeconds(60))
        assertEquals("2:34", formatElapsedSeconds(154))
    }

    @Test
    fun formatElapsedSeconds_zero() {
        assertEquals("0:00", formatElapsedSeconds(0))
    }
}
