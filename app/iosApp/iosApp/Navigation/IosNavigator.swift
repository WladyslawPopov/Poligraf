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
    
    // Combined state for both iPad (column visibility) and iPhone (sheet)
    @Published var isDrawerOpen: Bool = false
    
    // Native iPad-specific properties
    @Published var columnVisibility: NavigationSplitViewVisibility = .detailOnly
    @Published var preferredColumn: NavigationSplitViewColumn = .detail
    
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
    }
    
    func toggleDrawer() {
        setDrawerOpen(!isDrawerOpen)
    }
    
    func setDrawerOpen(_ isOpen: Bool) {
        withAnimation {
            self.isDrawerOpen = isOpen
            // Sync for iPad split view
            self.columnVisibility = isOpen ? .all : .detailOnly
            self.preferredColumn = isOpen ? .sidebar : .detail
        }
        onSyncDrawer(isOpen)
    }
    
    func push(route: NavRoute) {
        nativeNavStack?.push(route: route)
    }
    
    func pop() {
        nativeNavStack?.pop()
    }
}

struct IosNavHost<C: AnyObject, Content: View, DrawerViewContent: View>: View {
    @Environment(\.horizontalSizeClass) var sizeClass
    @ObservedObject var navigator: IosNavigator<C>
    let designSystem: DesignSystem
    let content: (C, IosNavigator<C>) -> Content
    let drawerView: (@escaping () -> Void) -> DrawerViewContent
    
    var body: some View {
        Group {
            if sizeClass == .compact {
                iphoneLayout
            } else {
                ipadLayout
            }
        }
        .environment(\.colorScheme, designSystem.isDark ? .dark : .light)
        .tint(IosTheme.color(.accentEnergy, from: designSystem))
    }
    
    // MARK: - iPhone (Native Sheet)
    private var iphoneLayout: some View {
        NavigationStack(path: $navigator.path) {
            rootView
                .navigationDestination(for: ComponentWrapper<C>.self) { wrapper in
                    content(wrapper.instance, navigator)
                }
        }
        .sheet(isPresented: $navigator.isDrawerOpen) {
            drawerView {
                navigator.setDrawerOpen(false)
            }
            .environment(\.colorScheme, designSystem.isDark ? .dark : .light)
            .presentationDetents([.medium, .large])
            .presentationDragIndicator(.visible)
            .presentationBackground {
                IosTheme.color(.surface, from: designSystem)
                    .opacity(0.8)
                    .background(.ultraThinMaterial)
            }
        }
    }
    
    // MARK: - iPad (Native SplitView)
    private var ipadLayout: some View {
        NavigationSplitView(
            columnVisibility: $navigator.columnVisibility,
            preferredCompactColumn: $navigator.preferredColumn
        ) {
            drawerView {
                navigator.setDrawerOpen(false)
            }
            .navigationSplitViewColumnWidth(min: 250, ideal: 300, max: 350)
        } detail: {
            NavigationStack(path: $navigator.path) {
                rootView
                    .navigationDestination(for: ComponentWrapper<C>.self) { wrapper in
                        content(wrapper.instance, navigator)
                    }
            }
        }
        .navigationSplitViewStyle(.balanced)
    }
    
    @ViewBuilder
    private var rootView: some View {
        if let root = navigator.root {
            content(root.instance, navigator)
        } else {
            ProgressView()
        }
    }
}
