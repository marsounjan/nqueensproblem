package com.marsounjan.nqueensproblem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.marsounjan.nqueensproblem.ui.NQueensApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AppServices.init(filesDir.absolutePath)

        setContent {
            NQueensApp()
        }
    }
}
