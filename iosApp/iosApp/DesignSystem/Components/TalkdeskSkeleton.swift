import SwiftUI

/// Loading placeholder with the geometry of `TalkdeskListRow` (64pt: disc, two text bars, time bar). Pulses between
/// full and half opacity every 2s; static when Reduce Motion is on.
struct TalkdeskListRowSkeleton: View {
    /// Widths of the title and sub-line bars as a fraction of the text column.
    var widths: (title: CGFloat, subtitle: CGFloat) = (0.62, 0.40)

    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var dimmed = false

    var body: some View {
        HStack(spacing: TalkdeskSpacing.s12) {
            Circle()
                .fill(TalkdeskColors.skeletonBase)
                .frame(width: TalkdeskSizes.avatar, height: TalkdeskSizes.avatar)
            VStack(alignment: .leading, spacing: TalkdeskSpacing.s8) {
                bar(TalkdeskColors.skeletonBase, height: 14, fraction: widths.title)
                bar(TalkdeskColors.skeletonHighlight, height: 12, fraction: widths.subtitle)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            RoundedRectangle(cornerRadius: 4)
                .fill(TalkdeskColors.skeletonBase)
                .frame(width: 36, height: 12)
        }
        .padding(.horizontal, TalkdeskSpacing.s16)
        .frame(height: TalkdeskSizes.row)
        .opacity(dimmed ? 0.5 : 1)
        .onAppear {
            guard !reduceMotion else { return }
            withAnimation(.easeInOut(duration: 1).repeatForever(autoreverses: true)) { dimmed = true }
        }
        .accessibilityHidden(true)
    }

    private func bar(_ color: Color, height: CGFloat, fraction: CGFloat) -> some View {
        GeometryReader { proxy in
            RoundedRectangle(cornerRadius: 4)
                .fill(color)
                .frame(width: proxy.size.width * fraction, height: height)
        }
        .frame(height: height)
    }
}

#Preview("Skeleton") {
    VStack(spacing: 0) {
        TalkdeskListRowSkeleton()
        TalkdeskListRowSkeleton(widths: (0.48, 0.55))
    }
    .talkdeskTheme()
}
