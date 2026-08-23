package com.marsounjan.nqueensproblem.ui.navigation

import androidx.navigation3.runtime.NavKey

/** Adapts a Navigation 3 back stack to the app's [DefaultNavigator] contract. */
class DefaultNavigator(private val backStack: MutableList<NavKey>) : Navigator {

    override fun open(route: NavigationRoute) {
        backStack.add(route)
    }

    override fun goBack() {
        backStack.removeLastOrNull()
    }
}

