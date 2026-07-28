import SwiftUI

struct RootView: View {
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea() // Hardware-level safety background
            
            ContentView()
                .ignoresSafeArea(edges: .all)
                .ignoresSafeArea(.keyboard)
        }
    }
}
