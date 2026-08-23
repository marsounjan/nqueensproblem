package com.marsounjan.nqueensproblem

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform