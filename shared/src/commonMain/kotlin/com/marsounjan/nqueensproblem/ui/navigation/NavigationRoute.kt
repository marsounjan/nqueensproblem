package com.marsounjan.nqueensproblem.ui.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

sealed interface NavigationRoute : NavKey {

    @Serializable
    data object Home : NavigationRoute

    @Serializable
    data class GameBoard(
        val boardSize: Int
    ) : NavigationRoute


}

val navKeyConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(NavigationRoute.Home::class, NavigationRoute.Home.serializer())
            subclass(NavigationRoute.GameBoard::class, NavigationRoute.GameBoard.serializer())
        }
    }
}

