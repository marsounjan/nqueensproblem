# N-Queens Problem

Hope I hit every point of the assignment and nothing slipped through the cracks. I ended up
building the iOS app too, purely because it was fun and cost me all of one extra iOS-specific
class to get there. A few things I had fun with, or made a point of getting right:

1. Picking the SFX :D
2. Smooth, efficient, millisecond-accurate elapsed time
3. Elapsed time pauses the moment the app goes to the background
4. App state survives being backgrounded (or killed) mid-game

https://github.com/user-attachments/assets/4a0f61c2-a2a5-4a8f-bbc4-1595c50b260a

## Project structure

- [`/shared`](./shared/src) — all app code (UI, view models, navigation, persistence, sound),
  shared across platforms.
  - [`commonMain`](./shared/src/commonMain/kotlin) — platform-independent code (the vast majority
    of the app).
  - [`androidMain`](./shared/src/androidMain/kotlin) / [`iosMain`](./shared/src/iosMain/kotlin) —
    platform-specific implementations behind `expect`/`actual` (currently just sound playback).
  - `commonTest` / `androidHostTest` — unit tests.
- [`/androidApp`](./androidApp) — thin Android application shell (`Activity` + `Application`)
  that hosts the shared UI.
- [`/iosApp`](./iosApp) — thin iOS application shell (SwiftUI `App` + a
  `UIViewControllerRepresentable` wrapper) that hosts the shared UI.

## How to build/run

**Android**

- From the Android Studio IDE: run the `androidApp` run configuration.
- From the command line: `./gradlew :androidApp:assembleDebug` (or `installDebug` with a
  connected device/emulator).

**iOS**

- From the Android Studio IDE: run the `iosApp` run configuration.
You may need to install xcode.

## How to test

All test code lives in `commonTest` and runs on every target, except for one Android-only
integration test for the DataStore-backed repository (`androidHostTest`) — there is no
iOS-specific test code.

- Run the shared test suite on the JVM/Android target (includes the DataStore integration test):
  `./gradlew :shared:testAndroidHostTest`
- Run the same shared test suite on the iOS simulator target:
  `./gradlew :shared:iosSimulatorArm64Test`

Tests cover the puzzle rules (`GameBoardState`), both view models (`GameViewModel`,
`HomeScreenViewModel`), time formatting, and the DataStore-backed best-times repository. View
model tests use fakes (`FakeNavigator`, `FakeSoundPlayer`, `FakeBestTimesRepository`) and a
manually-advanced `FakeTimeSource`, so elapsed-time behavior is deterministic and independent of
wall-clock time or the test dispatcher's virtual time.

## Architecture decisions

- **`AppServices` as a plain object, no DI framework.** Dependencies (`BestTimesRepository`,
  `SoundPlayer`) are held in a single `object` initialized once from each platform's entry point,
  and view models take them as constructor parameters. For a project this size, pulling in
  Koin/Hilt would add setup and indirection without a real payoff; the service locator gives the
  same testability (fakes passed directly to view model constructors) with none of that overhead.
- **Kotlin Multiplatform + Compose Multiplatform, one shared module.** UI, view models,
  navigation, persistence and sound all live in `shared`; the platform apps are just entry
  points. Only sound playback needs a real platform API, so that's the only `expect`/`actual`
  pair in the project.
- **MVVM with unidirectional state.** Each screen has a `ViewModel` exposing a single immutable
  `UiState` as a `StateFlow` (combined from smaller flows), and screens render that state and
  forward user intents back as function calls. `GameBoardState` itself is an immutable data
  class that derives `conflictingQueens`/`isSolved` from `queens`, so the puzzle rules are pure,
  synchronous, and trivially unit-testable without touching Compose or a `ViewModel`.
- **Navigation kept behind an interface.** `Navigator` is a small `open`/`goBack` contract with
  no dependency on Compose or the navigation library; `DefaultNavigator` is the real
  Navigation3-backed implementation, and tests use a `FakeNavigator`. It's held as a `ViewModel`
  so it keeps a stable identity across configuration changes. This also keeps view models fully
  testable in isolation: navigation is just a method call on a fake, not a one-off event the
  screen has to observe and forward, so there's no need for a separate event/effect channel back
  to the view.
- **Elapsed time as accumulated-plus-live-tick.** `GameViewModel` only stores elapsed time
  accumulated up to the last pause/win; while running, the exact time is computed on demand from
  a monotonic `TimeMark`. The UI (`GameScreen`) does its own per-frame ticking on top of that for
  a live display, so the view model doesn't need a repeating timer/coroutine of its own, and game
  progress (queens + elapsed time) survives process death via `SavedStateHandle`.
- **Persistence via DataStore, one fastest time per board size.** `BestTimesRepository` only
  tracks the single best time per size (not a history), backed by Jetpack DataStore
  (`Preferences`) so it works unchanged on both platforms.
- **Testing strategy.** Pure state/logic (`GameBoardState`, `formatGameTime`) is tested directly;
  view models are tested through fakes for every collaborator (navigation, sound, persistence,
  time), keeping tests fast and deterministic; the DataStore-backed repository has its own
  integration test against a real (temp-directory) `DataStore`.
