package com.marsounjan.nqueensproblem.util

import kotlinx.coroutines.flow.SharingStarted

val SharingStarted.Companion.WhileSubscribed5s: SharingStarted
    get() = WhileSubscribed(stopTimeoutMillis = 5_000)