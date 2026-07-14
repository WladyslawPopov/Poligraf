import SharedLogic
import SwiftUI

class RootComponentWrapper: ObservableObject {
    let component: RootComponent
    let navigator: IosNavigator<AnyObject>
    let designSystem: DesignSystem
    
    init() {
        // Just call the Kotlin factory - no parameters needed!
        let rootComp = IosComponentFactoryKt.createRootComponent()
        
        self.component = rootComp
        self.navigator = IosNavigator(navigator: rootComp.navigator)
        self.designSystem = DesignSystem(resources: IosResourceProvider(), isDark: true)
    }
}
