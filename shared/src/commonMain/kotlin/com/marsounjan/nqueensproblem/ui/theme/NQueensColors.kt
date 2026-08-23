package com.marsounjan.nqueensproblem.ui.theme

import androidx.compose.ui.graphics.Color

data class NQueensColors(
    val lightSquare: Color,
    val darkSquare: Color,
    val conflictSquare: Color,
    val queen: Color,
    val conflictQueen: Color,
)

val DefaultNQueensColors = NQueensColors(
    lightSquare = Color(0xFFEEEED2),
    darkSquare = Color(0xFF769656),
    conflictSquare = Color(0xFFE57373),
    queen = Color(0xFF212121),
    conflictQueen = Color(0xFFFFFFFF),
)
