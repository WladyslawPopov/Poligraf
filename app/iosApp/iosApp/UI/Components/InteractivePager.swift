import SwiftUI

/**
 * A professional, high-performance horizontal pager for iOS.
 * Implements "Zero-Bounce" clamping and Gemini-style visual transitions.
 */
struct InteractivePager<MenuView: View, Content: View>: View {
    @Binding var isOpen: Bool
    let menu: () -> MenuView
    let content: () -> Content
    
    @State private var offset: CGFloat = 0
    @State private var isDragging: Bool = false
    
    var body: some View {
        GeometryReader { geo in
            let fullWidth = geo.size.width
            let progress = min(1, max(0, offset / fullWidth))
            
            ZStack(alignment: .leading) {
                // 1. Menu Layer (Always interactive if visible)
                menu()
                    .scaleEffect(0.96 + (0.04 * progress))
                    .opacity(Double(progress))
                    .offset(x: (progress - 1) * 40)
                    .frame(width: fullWidth)
                    .ignoresSafeArea()
                
                // 2. Content Layer Group
                ZStack {
                    content()
                        .clipShape(RoundedRectangle(cornerRadius: progress * 32, style: .continuous))
                        .shadow(color: .black.opacity(Double(progress) * 0.5), radius: 12, x: -5, y: 0)
                    
                    // 3. Close Overlay (ONLY over the content layer)
                    if progress > 0.05 {
                        Color.black.opacity(0.01)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                setOpen(false, width: fullWidth)
                            }
                    }
                }
                .offset(x: offset)
                .disabled(isOpen && !isDragging) // Prevent clicks inside content when menu is open
            }
            .simultaneousGesture(
                DragGesture(minimumDistance: 15, coordinateSpace: .global)
                    .onChanged { value in
                        isDragging = true
                        let startX = value.startLocation.x
                        
                        // Edge swipe threshold for opening
                        if !isOpen && startX > 80 { return }
                        
                        let translation = value.translation.width
                        let newOffset = isOpen ? fullWidth + translation : translation
                        
                        self.offset = min(fullWidth, max(0, newOffset))
                    }
                    .onEnded { value in
                        isDragging = false
                        let velocity = value.predictedEndLocation.x - value.location.x
                        let translation = value.translation.width
                        
                        if (translation > 80 && !isOpen) || velocity > 400 {
                            setOpen(true, width: fullWidth)
                        } else if (translation < -80 && isOpen) || velocity < -400 {
                            setOpen(false, width: fullWidth)
                        } else {
                            setOpen(isOpen, width: fullWidth)
                        }
                    }
            )
            .onAppear {
                offset = isOpen ? fullWidth : 0
            }
            .onChange(of: isOpen) { _, newValue in
                if !isDragging {
                    withAnimation(.spring(response: 0.35, dampingFraction: 0.85)) {
                        offset = newValue ? fullWidth : 0
                    }
                }
            }
        }
        .background(Color.black) // Deep black base to kill any greyish light leaks
    }
    
    private func setOpen(_ open: Bool, width: CGFloat) {
        if isOpen != open {
            isOpen = open // This triggers IosNavigator.didSet
        }
        
        withAnimation(.spring(response: 0.35, dampingFraction: 0.85)) {
            offset = open ? width : 0
        }
    }
}
