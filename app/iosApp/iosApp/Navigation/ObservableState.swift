import SwiftUI
import SharedLogic

class ObservableState<T: AnyObject>: ObservableObject {
    @Published var value: T
    
    private var closeable: WatcherCloseable?
    
    init(_ watcher: StateWatcher<T>) {
        // Kotlin non-null types come as Optional in Swift headers
        self.value = watcher.value!
        
        self.closeable = watcher.watch { [weak self] newValue in
            DispatchQueue.main.async {
                self?.value = newValue!
            }
        }
    }
    
    deinit {
        closeable?.close()
    }
}
