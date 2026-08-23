package com.marsounjan.nqueensproblem.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.marsounjan.nqueensproblem.AppServices
import com.marsounjan.nqueensproblem.ui.game.GameViewModel
import com.marsounjan.nqueensproblem.ui.home.HomeScreenViewModel
import com.marsounjan.nqueensproblem.ui.game.GameScreen
import com.marsounjan.nqueensproblem.ui.home.HomeScreen

@Composable
fun Navigation() {
    val backStack = rememberNavBackStack(navKeyConfiguration, NavigationRoute.Home)
    val defaultNavigator = remember(backStack) { DefaultNavigator(backStack) }

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider
        {
            entry<NavigationRoute.Home> {
                val homeScreenViewModel = viewModel {
                    HomeScreenViewModel(
                        bestTimesRepository = AppServices.bestTimesRepository,
                        savedStateHandle = createSavedStateHandle()
                    )
                }
                HomeScreen(
                    viewModel = homeScreenViewModel,
                    navigator = defaultNavigator
                )
            }
            entry<NavigationRoute.GameBoard> { key ->
                val gameViewModel = viewModel {
                    GameViewModel(
                        boardSize = key.boardSize,
                        bestTimesRepository = AppServices.bestTimesRepository,
                        soundPlayer = AppServices.soundPlayer,
                        savedStateHandle = createSavedStateHandle(),
                    )
                }
                GameScreen(
                    viewModel = gameViewModel,
                    navigator = defaultNavigator
                )
            }
        },
    )
}