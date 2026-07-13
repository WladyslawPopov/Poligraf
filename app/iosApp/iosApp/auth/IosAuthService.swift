import Foundation
import FirebaseAuth
import SharedLogic

class IosAuthService: AuthService {
    func signInAnonymously() async throws -> KmpResult<KotlinUnit> {
        do {
            _ = try await Auth.auth().signInAnonymously()
            return KmpResultSuccess(data: KotlinUnit())
        } catch {
            return KmpResultError(throwable: KotlinThrowable(message: error.localizedDescription))
        }
    }
    
    func getIdToken() async throws -> String? {
        return try await Auth.auth().currentUser?.getIDToken()
    }
    
    func isAuthorized() -> Bool {
        return Auth.auth().currentUser != nil
    }
}
