import Foundation
import SharedLogic

class IosIntegrityImpl: DeviceIntegrity {
    func __checkIntegrity() async throws -> KmpResult<KotlinUnit> {
        return KmpResultSuccess(data: KotlinUnit())
    }
}

class IosReviewManagerImpl: ReviewManager {
    func __requestReview() async throws -> KmpResult<KotlinUnit> {
        return KmpResultSuccess(data: KotlinUnit())
    }
}
