import SwiftUI
import SharedLogic

/**
 * A generic Swift wrapper that turns a Kotlin StateWatcher into a SwiftUI ObservableObject.
 */
class ObservableState<T: AnyObject>: ObservableObject {
    @Published var value: T
    
    private var closeable: WatcherCloseable?
    
    init(_ watcher: StateWatcher<T>) {
        // Force unwrap because Kotlin StateFlow always has a value.
        // If watcher.value is nil here, check if the KMP side is initialized.
        self.value = watcher.value!
        
        self.closeable = watcher.watch { [weak self] newValue in
            DispatchQueue.main.async {
                if let val = newValue {
                    self?.value = val
                }
            }
        }
    }
    
    deinit {
        closeable?.close()
    }
}
