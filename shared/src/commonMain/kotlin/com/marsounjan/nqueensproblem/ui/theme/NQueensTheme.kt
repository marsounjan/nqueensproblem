package com.marsounjan.nqueensproblem.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalNQueensColors = staticCompositionLocalOf { DefaultNQueensColors }

@Composable
fun NQueensTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        CompositionLocalProvider(LocalNQueensColors provides DefaultNQueensColors) {
            content()
        }
    }
}

/** Mirrors [MaterialTheme]'s object+function pairing: [NQueensTheme] provides, this reads. */
object NQueensTheme {
    val colors: NQueensColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNQueensColors.current
}
