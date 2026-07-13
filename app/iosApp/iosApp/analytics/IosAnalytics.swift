import Foundation
import FirebaseAnalytics
import SharedLogic

class IosAnalytics: SharedLogic.Analytics {
    func logEvent(name: String, params: [String : Any]) {
        Analytics.logEvent(name, parameters: params)
    }
    
    func setUserProperty(name: String, value: String) {
        Analytics.setUserProperty(value, forName: name)
    }
}
