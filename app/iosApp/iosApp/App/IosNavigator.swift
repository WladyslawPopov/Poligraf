import Foundation
import SharedLogic

class IosNavigator: ObservableObject, AppNavigation {
    @Published var path: [AppRoute] = []
    @Published var isDrawerOpen: Bool = false
    @Published var isDark: Bool = true
    
    // MARK: - AppNavigation implementation
    
    func openMain() {
        if NavigationGlobalLock.shared.canNavigate() {
            path = []
        }
    }
    
    func openDebug() {
        if NavigationGlobalLock.shared.canNavigate() {
            let route = AppRoute.Debug()
            if path.last != route {
                path.append(route)
            }
        }
    }
    
    func openRecording(subjectId: String) {
        if NavigationGlobalLock.shared.canNavigate() {
            let route = AppRoute.Recording(subjectId: subjectId)
            if path.last != route {
                path.append(route)
            }
        }
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
