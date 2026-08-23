package com.marsounjan.nqueensproblem.ui.navigation

/**
 * The app's navigation intents, expressed without any dependency on a navigation library or on
 * Compose - ViewModels depend only on this interface, so navigation behavior is testable with a
 * fake, independent of the UI layer.
 */
interface Navigator {
    fun open(route: NavigationRoute)
    fun goBack()
}