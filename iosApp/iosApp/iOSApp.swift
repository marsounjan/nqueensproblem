import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.initializeAppServices()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}