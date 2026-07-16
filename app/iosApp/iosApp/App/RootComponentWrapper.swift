import SharedLogic
import SwiftUI

class RootComponentWrapper: ObservableObject {
    let component: RootComponent
    let navigator: IosNavigator<AnyObject>
    
    @Published var isDark: Bool = true
    
    var designSystem: DesignSystem {
        DesignSystem(resources: IosResourceProvider(), isDark: isDark)
    }
    
    init() {
        // Just call the Kotlin factory - no parameters needed!
        let rootComp = IosComponentFactoryKt.createRootComponent()
        
        self.component = rootComp
        self.navigator = IosNavigator(navigator: rootComp.navigator)
    }
    
    func toggleTheme() {
        isDark.toggle()
    }
}
