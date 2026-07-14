import SwiftUI
import SharedLogic

struct ComponentWrapper<C: AnyObject>: Hashable, Identifiable {
    let id: String
    let instance: C
    
    init(instance: C) {
        self.instance = instance
        self.id = "\(ObjectIdentifier(instance).hashValue)"
    }
    
    static func == (lhs: ComponentWrapper<C>, rhs: ComponentWrapper<C>) -> Bool {
        lhs.id == rhs.id
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }
}

class IosNavigator<C: AnyObject>: ObservableObject {
    @Published var path: [ComponentWrapper<C>] = []
    @Published var root: ComponentWrapper<C>? = nil
    
    private var nativeNavStack: SharedLogic.NativeNavStack<C>?
    
    init(navigator: AppNavigator) {
        self.nativeNavStack = SharedLogic.NativeNavStack(navigator: navigator) { [weak self] newStack in
            DispatchQueue.main.async {
                guard let self = self else { return }
                let wrapped = newStack.map { ComponentWrapper(instance: $0) }
                self.root = wrapped.first
                self.path = Array(wrapped.dropFirst())
            }
        }
    }
    
    func push(route: NavRoute) {
        nativeNavStack?.push(route: route)
    }
    
    func pop() {
        nativeNavStack?.pop()
    }
}

struct IosNavHost<C: AnyObject, Content: View>: View {
    @ObservedObject var navigator: IosNavigator<C>
    let content: (C) -> Content
    
    var body: some View {
        NavigationStack(path: $navigator.path) {
            Group {
                if let root = navigator.root {
                    content(root.instance)
                } else {
                    VStack {
                        ProgressView()
                        Text("Loading...")
                    }
                }
            }
            .navigationDestination(for: ComponentWrapper<C>.self) { wrapper in
                content(wrapper.instance)
            }
        }
    }
}
