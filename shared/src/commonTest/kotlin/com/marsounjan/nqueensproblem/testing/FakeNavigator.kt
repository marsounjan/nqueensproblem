package com.marsounjan.nqueensproblem.testing

import com.marsounjan.nqueensproblem.ui.navigation.NavigationRoute
import com.marsounjan.nqueensproblem.ui.navigation.Navigator

class FakeNavigator : Navigator {
    val openGameCalls = mutableListOf<Int>()
    var goBackCallCount = 0
        private set

    override fun open(route: NavigationRoute) {
        if (route is NavigationRoute.GameBoard) {
            openGameCalls += route.boardSize
        }
    }

    override fun goBack() {
        goBackCallCount++
    }
}
