import SwiftUI
import SharedLogic

struct ContentView: View {
    var body: some View {
        VStack {
            Image(systemName: "mic.circle.fill")
                .font(.system(size: 100))
                .foregroundColor(.blue)
            Text("LieDetector Native iOS")
                .font(.title)
            Text("Architecture Ready")
                .font(.subheadline)
        }
        .padding()
    }
}
