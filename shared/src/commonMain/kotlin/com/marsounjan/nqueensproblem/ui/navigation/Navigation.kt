package com.marsounjan.nqueensproblem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.marsounjan.nqueensproblem.AppServices
import com.marsounjan.nqueensproblem.ui.game.GameScreen
import com.marsounjan.nqueensproblem.ui.game.GameViewModel
import com.marsounjan.nqueensproblem.ui.home.HomeScreen
import com.marsounjan.nqueensproblem.ui.home.HomeScreenViewModel

@Composable
fun Navigation() {
    val backStack = rememberNavBackStack(navKeyConfiguration, NavigationRoute.Home)
    val navigator = viewModel { DefaultNavigator() }
    SideEffect { navigator.backStack = backStack }

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider
        {
            entry<NavigationRoute.Home> {
                HomeScreen(
                    viewModel = viewModel {
                        HomeScreenViewModel(
                            bestTimesRepository = AppServices.bestTimesRepository,
                            navigator = navigator,
                            savedStateHandle = createSavedStateHandle()
                        )
                    }
                )
            }
            entry<NavigationRoute.GameBoard> { key ->
                GameScreen(
                    viewModel = viewModel {
                        GameViewModel(
                            boardSize = key.boardSize,
                            bestTimesRepository = AppServices.bestTimesRepository,
                            soundPlayer = AppServices.soundPlayer,
                            navigator = navigator,
                            savedStateHandle = createSavedStateHandle(),
                        )
                    }
                )
            }
        },
    )
}