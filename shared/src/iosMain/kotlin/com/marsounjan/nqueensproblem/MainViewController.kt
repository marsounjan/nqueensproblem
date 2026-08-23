package com.marsounjan.nqueensproblem

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.marsounjan.nqueensproblem.ui.NQueensApp
import platform.Foundation.NSHomeDirectory

fun MainViewController() = ComposeUIViewController {
    remember { AppServices.init(NSHomeDirectory() + "/Documents") }

    NQueensApp()
}
