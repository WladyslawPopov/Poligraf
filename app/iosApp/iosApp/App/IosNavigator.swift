import Foundation
import SharedLogic

class IosNavigator: ObservableObject, AppNavigation {
    @Published var path: [AppRoute] = []
    @Published var isDrawerOpen: Bool = false
    @Published var isDark: Bool = true
    
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
        isDrawerOpen.toggle()
    }
    
    func setDrawerOpen(isOpen: Bool) {
        isDrawerOpen = isOpen
    }
    
    func toggleTheme() {
        isDark.toggle()
    }
}
