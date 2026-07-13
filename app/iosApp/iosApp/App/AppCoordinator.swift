import Foundation
import UIKit
import SharedLogic

class AppCoordinator {
    static let shared = AppCoordinator()
    
    private var appDelegate: AppDelegate?
    
    func setAppDelegate(_ delegate: AppDelegate) {
        self.appDelegate = delegate
    }
}
