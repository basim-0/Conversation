import SwiftUI

/// 4pt progress bar (`ta-progress`) on `track` with a `track-fill` fill.
///
/// - Determinate: pass `value` (0…1). `isThreshold` switches the fill to `status-error` when a time
///   limit is near.
/// - Indeterminate: pass `nil`; a 40% segment sweeps across (1.2s). Static at half opacity when
///   Reduce Motion is on.
struct TalkdeskLinearProgress: View {
    var value: Double?
    var isThreshold: Bool = false

    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var phase: CGFloat = -0.4

    var body: some View {
        let fill = isThreshold ? TalkdeskColors.statusError : TalkdeskColors.trackFill
        GeometryReader { proxy in
            let width = proxy.size.width
            ZStack(alignment: .leading) {
                Capsule().fill(TalkdeskColors.track)
                if let value {
                    Capsule().fill(fill).frame(width: width * min(max(value, 0), 1))
                } else if reduceMotion {
                    Capsule().fill(fill.opacity(0.5))
                } else {
                    Capsule()
                        .fill(fill)
                        .frame(width: width * 0.4)
                        .offset(x: width * phase)
                        .onAppear {
                            withAnimation(.timingCurve(0.2, 0, 0, 1, duration: 1.2).repeatForever(autoreverses: false)) {
                                phase = 1
                            }
                        }
                }
            }
            .clipShape(Capsule())
        }
        .frame(height: 4)
        .accessibilityElement()
        .accessibilityAddTraits(.updatesFrequently)
        .accessibilityValue(value.map { Text($0, format: .percent.precision(.fractionLength(0))) } ?? Text(verbatim: ""))
    }
}

/// 24pt circular spinner (`ta-spinner`): 3pt `track` ring with a rotating `track-fill` quarter
/// (0.8s). Static when Reduce Motion is on.
struct TalkdeskSpinner: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var rotation: Double = 0

    var body: some View {
        ZStack {
            Circle().stroke(TalkdeskColors.track, lineWidth: 3)
            Circle()
                .trim(from: 0, to: 0.25)
                .stroke(TalkdeskColors.trackFill, lineWidth: 3)
                .rotationEffect(.degrees(rotation - 135))
        }
        .padding(1.5)
        .frame(width: 24, height: 24)
        .onAppear {
            guard !reduceMotion else { return }
            withAnimation(.linear(duration: 0.8).repeatForever(autoreverses: false)) { rotation = 360 }
        }
        .accessibilityHidden(true)
    }
}

#Preview("Progress") {
    VStack(spacing: 16) {
        TalkdeskLinearProgress(value: 0.6)
        TalkdeskLinearProgress(value: 0.85, isThreshold: true)
        TalkdeskLinearProgress(value: nil)
        TalkdeskSpinner()
    }
    .padding()
    .talkdeskTheme()
}
