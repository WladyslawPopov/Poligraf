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
    
    // Catch ANY change to sync with Kotlin and trigger haptics
    @Published var isDrawerOpen: Bool = false {
        didSet {
            if oldValue != isDrawerOpen {
                impact.impactOccurred()
                onSyncDrawer(isDrawerOpen)
            }
        }
    }
    
    private let impact = UIImpactFeedbackGenerator(style: .medium)
    private var nativeNavStack: SharedLogic.NativeNavStack<C>?
    private let onSyncDrawer: (Bool) -> Void
    
    init(navigator: AppNavigator, onSyncDrawer: @escaping (Bool) -> Void = { _ in }) {
        self.onSyncDrawer = onSyncDrawer
        self.nativeNavStack = SharedLogic.NativeNavStack(navigator: navigator) { [weak self] newStack in
            DispatchQueue.main.async {
                guard let self = self else { return }
                let wrapped = newStack.map { ComponentWrapper(instance: $0) }
                self.root = wrapped.first
                self.path = Array(wrapped.dropFirst())
            }
        }
        impact.prepare()
    }
    
    func toggleDrawer() {
        isDrawerOpen.toggle()
    }
    
    func setDrawerOpen(_ isOpen: Bool) {
        if isDrawerOpen != isOpen {
            isDrawerOpen = isOpen
        }
    }
    
    func push(route: NavRoute) {
        nativeNavStack?.push(route: route)
    }
    
    func pop() {
        nativeNavStack?.pop()
    }
}

struct IosNavHost<C: AnyObject, Content: View, DrawerView: View>: View {
    @ObservedObject var navigator: IosNavigator<C>
    let designSystem: DesignSystem
    let content: (C, IosNavigator<C>) -> Content // Pass navigator to sub-views
    let drawerView: () -> DrawerView
    
    var body: some View {
        InteractivePager(
            isOpen: $navigator.isDrawerOpen,
            menu: drawerView,
            content: {
                NavigationStack(path: $navigator.path) {
                    Group {
                        if let root = navigator.root {
                            content(root.instance, navigator)
                        } else {
                            ProgressView()
                        }
                    }
                    .navigationDestination(for: ComponentWrapper<C>.self) { wrapper in
                        content(wrapper.instance, navigator)
                    }
                    .scrollContentBackground(.hidden)
                }
                .scrollDisabled(!navigator.path.isEmpty)
            }
        )
    }
}
