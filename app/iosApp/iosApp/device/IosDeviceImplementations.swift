import Foundation
import SharedLogic

class IosIntegrityImpl: DeviceIntegrity {
    func __checkIntegrity() async throws -> KotlinBoolean {
        return KotlinBoolean(bool: true)
    }
}

class IosReviewManagerImpl: ReviewManager {
    func __requestReview() async throws -> KotlinBoolean {
        return KotlinBoolean(bool: true)
    }
}
