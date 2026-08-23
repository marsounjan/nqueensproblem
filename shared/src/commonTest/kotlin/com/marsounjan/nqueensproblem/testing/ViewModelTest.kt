package com.marsounjan.nqueensproblem.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

/**
 * ViewModels use `viewModelScope`, which dispatches on `Dispatchers.Main`. Tests install an
 * unconfined test dispatcher as Main so `stateIn(..., SharingStarted.Eagerly, ...)` and any
 * ticker coroutines run against the test's own virtual-time scheduler instead of a real Main
 * loop that doesn't exist in a unit test.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class ViewModelTest {

    protected val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun installTestMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun uninstallTestMainDispatcher() {
        Dispatchers.resetMain()
    }
}
