import SharedLogic
import SwiftUI

class RootComponentWrapper: ObservableObject {
    let component: RootComponent
    let navigator: IosNavigator<AnyObject>
    
    @Published var isDark: Bool = true
    
    private var drawerCloseable: WatcherCloseable?
    
    var designSystem: DesignSystem {
        DesignSystem(resources: IosResourceProvider(), isDark: isDark)
    }
    
    init() {
        let rootComp = IosComponentFactoryKt.createRootComponent()
        
        let iosNavigator = IosNavigator<AnyObject>(
            navigator: rootComp.navigator,
            onSyncDrawer: { isOpen in
                // Sync back to Kotlin only if values differ to avoid loops
                let currentKmpValue = rootComp.navigator.isDrawerOpen.value as? Bool ?? false
                if currentKmpValue != isOpen {
                    rootComp.navigator.setDrawerOpen(isOpen: isOpen)
                }
            }
        )
        
        self.component = rootComp
        self.navigator = iosNavigator
        
        setupObservers()
    }
    
    private func setupObservers() {
        // Sync drawer state from Kotlin to iOS
        drawerCloseable = component.drawerOpenWatcher.watch { [weak self] isOpen in
            guard let isOpen = isOpen?.boolValue else { return }
            DispatchQueue.main.async {
                // Only update if the native state is different from what we already have
                if self?.navigator.isDrawerOpen != isOpen {
                    self?.navigator.setDrawerOpen(isOpen)
                }
            }
        }
    }
    
    func toggleTheme() {
        isDark.toggle()
    }
    
    func toggleDrawer() {
        navigator.toggleDrawer()
    }
    
    deinit {
        drawerCloseable?.close()
    }
}
