import Foundation
import FirebaseAuth
import SharedLogic

class IosAuthService: AuthService {
    
    func __signInAnonymously() async {
        do {
            _ = try await Auth.auth().signInAnonymously()
        } catch {}
    }
    
    func __getIdToken() async throws -> String? {
        return try await Auth.auth().currentUser?.getIDToken()
    }
    
    func isAuthorized() -> Bool {
        return Auth.auth().currentUser != nil
    }
}
