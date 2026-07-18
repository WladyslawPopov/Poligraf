import SwiftUI
import SharedLogic

/**
 * A generic Swift wrapper that turns a Kotlin StateWatcher into a SwiftUI ObservableObject.
 * Handles nullable Kotlin states by making the value optional.
 */
class ObservableState<T: AnyObject>: ObservableObject {
    @Published var value: T?
    
    private var closeable: WatcherCloseable?
    
    init(_ watcher: StateWatcher<T>) {
        // Safe assignment - Kotlin StateFlow might be initialized with null
        self.value = watcher.value
        
        self.closeable = watcher.watch { [weak self] newValue in
            DispatchQueue.main.async {
                self?.value = newValue
            }
        }
    }
    
    deinit {
        closeable?.close()
    }
}
