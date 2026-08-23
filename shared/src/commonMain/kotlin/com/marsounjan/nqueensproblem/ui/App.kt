package com.marsounjan.nqueensproblem.ui

import androidx.compose.runtime.Composable
import com.marsounjan.nqueensproblem.ui.navigation.Navigation
import com.marsounjan.nqueensproblem.ui.theme.NQueensTheme

@Composable
fun NQueensApp() {
    NQueensTheme {
        Navigation()
    }
}
