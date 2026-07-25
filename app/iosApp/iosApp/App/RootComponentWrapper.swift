import SharedLogic
import SwiftUI

class RootComponentWrapper: ObservableObject, AppNavigation {
    private(set) var component: RootComponent!
    
    @Published var isDark: Bool = true
    @Published var path: [AppRoute] = []
    @Published var isDrawerOpen: Bool = false
    
    private var drawerCloseable: WatcherCloseable?
    
    // Simple cache for ViewModels to avoid recreation on view updates
    private var mainComponent: MainComponent?
    private var debugComponent: DebugComponent?
 
    var designSystem: DesignSystem {
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif
        return DesignSystem(resources: IosResourceProvider(), isDark: isDark, isDebug: isDebug)
    }
    
    init() {
        // Since component is implicitly unwrapped, we can now safely use 'self' 
        // to initialize the Kotlin component which requires the navigation interface.
        let rootComp = IosComponentFactoryKt.createRootComponent(navigation: self)
        self.component = rootComp
        setupObservers()
    }
    
    // MARK: - AppNavigation implementation
    
    func openMain() {
        path = []
    }
    
    func openDebug() {
        path.append(AppRoute.Debug())
    }
    
    func openInvestigation(subjectId: String) {
        path.append(AppRoute.Investigation(subjectId: subjectId))
    }
    
    func back() {
        if !path.isEmpty {
            path.removeLast()
        }
    }
    
    func toggleDrawer() {
        component.toggleDrawer()
    }
    
    func setDrawerOpen(isOpen: Bool) {
        component.setDrawerOpen(isOpen: isOpen)
    }

    // MARK: - Component Accessors
    
    func getMainComponent() -> MainComponent {
        if let cached = mainComponent { return cached }
        let comp = component.createMainComponent()
        mainComponent = comp
        return comp
    }
    
    func getDebugComponent() -> DebugComponent {
        if let cached = debugComponent { return cached }
        let comp = component.createDebugComponent()
        debugComponent = comp
        return comp
    }

    private func setupObservers() {
        // Sync drawer state from Kotlin to iOS
        drawerCloseable = component.drawerOpenWatcher.watch { [weak self] isOpen in
            guard let isOpen = isOpen?.boolValue else { return }
            DispatchQueue.main.async {
                if self?.isDrawerOpen != isOpen {
                    self?.isDrawerOpen = isOpen
                }
            }
        }
    }
    
    func toggleTheme() {
        isDark.toggle()
    }
    
    deinit {
        drawerCloseable?.close()
        component.onDestroy()
    }
}
