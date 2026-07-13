import Foundation
import SharedLogic

class IosIntegrityImpl: DeviceIntegrity {
    func checkIntegrity() async throws -> KmpResult<KotlinUnit> {
        return KmpResultSuccess(data: KotlinUnit())
    }
}

class IosReviewManagerImpl: ReviewManager {
    func requestReview() async throws -> KmpResult<KotlinUnit> {
        return KmpResultSuccess(data: KotlinUnit())
    }
}
