import SwiftUI

/// First load: eight skeleton rows with the real row geometry.
struct ActivityLoadingView: View {
    private let widths: [(title: CGFloat, subtitle: CGFloat)] = [
        (0.62, 0.40), (0.48, 0.55), (0.70, 0.35), (0.55, 0.45),
        (0.66, 0.38), (0.44, 0.52), (0.60, 0.42), (0.52, 0.36),
    ]

    var body: some View {
        VStack(spacing: 0) {
            ForEach(widths.indices, id: \.self) { index in
                TalkdeskListRowSkeleton(widths: widths[index])
            }
        }
        .padding(.top, TalkdeskSpacing.s12)
        .frame(maxWidth: .infinity, alignment: .top)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel("Loading activity")
        .accessibilityAddTraits(.updatesFrequently)
    }
}

/// Empty and error states of the Activity list: the DS empty state centred in the free space.
struct ActivityMessageView: View {
    let icon: Image
    let title: LocalizedStringKey
    let message: LocalizedStringKey
    var actionTitle: LocalizedStringKey?
    var action: (() -> Void)?

    var body: some View {
        TalkdeskEmptyState(icon: icon, title: title, message: message, actionTitle: actionTitle, action: action)
            .frame(maxWidth: .infinity, minHeight: 420)
    }
}
