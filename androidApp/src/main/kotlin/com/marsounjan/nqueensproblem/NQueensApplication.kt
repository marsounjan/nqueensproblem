package com.marsounjan.nqueensproblem

import android.app.Application

class NQueensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppServices.init(filesDir.absolutePath)
    }
}
