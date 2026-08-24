package com.marsounjan.nqueensproblem

import androidx.compose.ui.window.ComposeUIViewController
import com.marsounjan.nqueensproblem.ui.NQueensApp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

fun MainViewController() = ComposeUIViewController {
    NQueensApp()
}

fun initializeAppServices() {
    AppServices.init(applicationSupportDirectory())
}

@OptIn(ExperimentalForeignApi::class)
private fun applicationSupportDirectory(): String {
    val path = NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
        .first() as String
    NSFileManager.defaultManager.createDirectoryAtPath(path, true, null, null)
    return path
}
