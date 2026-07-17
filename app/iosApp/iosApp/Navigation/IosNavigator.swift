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
    @Published var isDrawerOpen: Bool = false // Global drawer state
    
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
    
    func toggleDrawer() {
        withAnimation(.spring(response: 0.3, dampingFraction: 0.8)) {
            isDrawerOpen.toggle()
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
    let content: (C) -> Content
    let drawerView: () -> DrawerView // Slot for the drawer
    
    init(
        navigator: IosNavigator<C>,
        designSystem: DesignSystem,
        @ViewBuilder content: @escaping (C) -> Content,
        @ViewBuilder drawerView: @escaping () -> DrawerView
    ) {
        self.navigator = navigator
        self.designSystem = designSystem
        self.content = content
        self.drawerView = drawerView
    }
    
    var body: some View {
        ZStack {
            // 1. Global Persistent Background
            ScalesView(designSystem: designSystem)
                .ignoresSafeArea()

            // 2. Navigation Layer (Tilts when drawer is open)
            NavigationStack(path: $navigator.path) {
                Group {
                    if let root = navigator.root {
                        content(root.instance)
                    } else {
                        ProgressView()
                    }
                }
                .navigationDestination(for: ComponentWrapper<C>.self) { wrapper in
                    content(wrapper.instance)
                }
                .scrollContentBackground(.hidden)
            }
            .disabled(navigator.isDrawerOpen)
            .scaleEffect(navigator.isDrawerOpen ? 0.92 : 1.0)
            .rotation3DEffect(
                .degrees(navigator.isDrawerOpen ? -10 : 0),
                axis: (x: 0, y: 1, z: 0)
            )
            .blur(radius: navigator.isDrawerOpen ? 4 : 0)

            // 3. Interaction Overlay
            if navigator.isDrawerOpen {
                Color.black.opacity(0.4)
                    .ignoresSafeArea()
                    .onTapGesture {
                        navigator.toggleDrawer()
                    }
                    .transition(.opacity)
            }

            // 4. Side Menu (Stable on top)
            if navigator.isDrawerOpen {
                HStack(spacing: 0) {
                    drawerView()
                        .frame(width: 280)
                        .transition(.move(edge: .leading))
                    Spacer()
                }
                .ignoresSafeArea()
            }
        }
        .animation(.spring(response: 0.3, dampingFraction: 0.8), value: navigator.isDrawerOpen)
    }
}
