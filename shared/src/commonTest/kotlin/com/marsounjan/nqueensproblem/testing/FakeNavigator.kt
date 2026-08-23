package com.marsounjan.nqueensproblem.testing

import com.marsounjan.nqueensproblem.ui.navigation.Navigator

class FakeNavigator : Navigator {
    val openGameCalls = mutableListOf<Int>()
    var goBackCallCount = 0
        private set

    override fun open(boardSize: Int) {
        openGameCalls += boardSize
    }

    override fun goBack() {
        goBackCallCount++
    }
}
