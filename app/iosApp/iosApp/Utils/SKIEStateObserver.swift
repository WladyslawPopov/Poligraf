import SwiftUI
import SharedLogic

/**
 * A generic Swift wrapper that turns a SKIE-generated StateFlow into a SwiftUI ObservableObject.
 * Supports both regular and optional StateFlows.
 */
class SKIEStateObserver<T>: ObservableObject {
    @Published var value: T
    
    private var task: Task<Void, Never>?
    
    // Initializer for non-optional StateFlow
    init(_ flow: SkieSwiftStateFlow<T>) {
        self.value = flow.value
        
        self.task = Task { @MainActor in
            for await newValue in flow {
                self.value = newValue
            }
        }
    }
    
    deinit {
        task?.cancel()
    }
}

/**
 * Specialized observer for Optional StateFlows from Kotlin (T?).
 */
class SKIEOptionalStateObserver<T>: ObservableObject {
    @Published var value: T?
    
    private var task: Task<Void, Never>?
    
    init(_ flow: SkieSwiftOptionalStateFlow<T>) {
        self.value = flow.value
        
        self.task = Task { @MainActor in
            for await newValue in flow {
                self.value = newValue
            }
        }
    }
    
    deinit {
        task?.cancel()
    }
}
