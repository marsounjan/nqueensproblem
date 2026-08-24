package com.marsounjan.nqueensproblem.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/** Adapts a Navigation 3 back stack to the app's [Navigator] contract.
 *
 * Held as a [ViewModel] (rather than `remember`ed in the Composition) so it survives
 * configuration changes with a stable identity - screen ViewModels capture this instance once
 * and it must stay valid even after the Activity/Composition is torn down and rebuilt. Its
 * [backStack] pointer is kept in sync with the current [NavBackStack] by [Navigation].
 */
class DefaultNavigator : ViewModel(), Navigator {

    var backStack: NavBackStack<NavKey> = NavBackStack()

    override fun open(route: NavigationRoute) {
        backStack.add(route)
    }

    override fun goBack() {
        backStack.removeLastOrNull()
    }
}

