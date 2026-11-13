import SwiftUI

/// Animated splash that hands off to the first screen ("Splash · Android and iOS" board).
///
/// The first frame matches the launch screen (`action-primary` with the 96pt white mark, dots at 35%),
/// so there is no visible cut. Then:
/// - 300ms: the three dots pulse in turn (300ms each, 150ms stagger) for at least two passes, and keep
///   pulsing until `isReady`; the current pass always finishes. "Talkdesk Conversations" fades up 8pt.
/// - Handoff (500ms ease-in-out): the mark shrinks 96→40 and glides onto the login header's mark, its
///   colours invert, and the background turns `bg-window`. The name fades out.
/// - 300ms into the handoff, `content` fades up 8pt (300ms ease-out).
///
/// With Reduce Motion: the static launch frame (mark only) crossfades to `content` in 200ms once `isReady`.
/// `content` is built from the start (hidden), so its state survives the transition.
struct SplashTransition<Content: View>: View {
    var isReady: Bool
    var onFinished: () -> Void = {}
    @ViewBuilder var content: () -> Content

    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @State private var start = Date.now
    @State private var readyAt: Double?
    @State private var finished = false

    var body: some View {
        TimelineView(.animation(minimumInterval: nil, paused: finished)) { timeline in
            let clock = SplashClock(
                elapsed: max(0, timeline.date.timeIntervalSince(start) * 1000),
                readyAt: readyAt,
                reduceMotion: reduceMotion
            )
            let contentShift = finished || reduceMotion ? 0 : TalkdeskSpacing.s8 * (1 - clock.contentProgress)

            ZStack {
                if !finished {
                    TalkdeskColors.actionPrimary
                        .mix(with: TalkdeskColors.bgWindow, by: reduceMotion ? 1 : clock.handoffProgress)
                        .ignoresSafeArea()
                }
                content()
                    .environment(\.appMarkHidden, !finished)
                    .opacity(finished || reduceMotion ? 1 : clock.contentProgress)
                    // A visual effect, so the header's anchor keeps its resting position.
                    .visualEffect { view, _ in view.offset(y: contentShift) }
                    .accessibilityHidden(!finished)
            }
            .overlayPreferenceValue(AppMarkAnchorKey.self) { anchor in
                if !finished {
                    GeometryReader { proxy in
                        SplashOverlay(clock: clock, target: anchor.map { proxy[$0] }, size: proxy.size)
                    }
                    .ignoresSafeArea()
                }
            }
        }
        .onChange(of: isReady, initial: true) { _, ready in
            if ready, readyAt == nil { readyAt = Date.now.timeIntervalSince(start) * 1000 }
        }
        .task(id: readyAt) {
            guard !finished,
                  let end = SplashClock(elapsed: 0, readyAt: readyAt, reduceMotion: reduceMotion).endTime
            else { return }
            let remaining = end - Date.now.timeIntervalSince(start) * 1000
            if remaining > 0 { try? await Task.sleep(for: .milliseconds(Int(remaining.rounded(.up)))) }
            guard !Task.isCancelled else { return }
            finished = true
            onFinished()
        }
    }
}

/// The moving mark, the name and (with Reduce Motion) the brand background.
private struct SplashOverlay: View {
    let clock: SplashClock
    let target: CGRect?
    let size: CGSize

    var body: some View {
        let reduce = clock.reduceMotion
        let p = reduce ? 0 : clock.handoffProgress
        ZStack(alignment: .top) {
            Canvas { context, canvasSize in
                if reduce {
                    context.fill(Path(CGRect(origin: .zero, size: canvasSize)), with: .color(TalkdeskColors.actionPrimary))
                }
                let side = lerp(TalkdeskSizes.avatarLarge, TalkdeskSizes.avatar, p)
                let startCenter = CGPoint(x: canvasSize.width / 2, y: canvasSize.height / 2)
                let center = target.map { lerp(startCenter, CGPoint(x: $0.midX, y: $0.midY), p) } ?? startCenter
                let radius = lerp(TalkdeskRadius.xl, TalkdeskRadius.md, p)
                let glyphSide = lerp(TalkdeskSpacing.s56, TalkdeskSizes.icon, p)
                let markAlpha = target == nil ? 1 - p : 1

                let markRect = CGRect(x: center.x - side / 2, y: center.y - side / 2, width: side, height: side)
                context.fill(
                    RoundedRectangle(cornerRadius: radius).path(in: markRect),
                    with: .color(TalkdeskColors.textOnBrand.mix(with: TalkdeskColors.actionPrimary, by: p).opacity(markAlpha))
                )
                var dotAlpha = [Double](repeating: 1, count: 3)
                var dotLift = [CGFloat](repeating: 0, count: 3)
                if !reduce {
                    for i in 0..<3 {
                        let pulse = clock.dotPulse(i)
                        dotAlpha[i] = lerp(SplashClock.dotRestAlpha + (1 - SplashClock.dotRestAlpha) * pulse, 1, p) * markAlpha
                        dotLift[i] = SplashClock.dotLift * pulse
                    }
                }
                AppMarkGlyph.draw(
                    in: context,
                    rect: CGRect(x: center.x - glyphSide / 2, y: center.y - glyphSide / 2, width: glyphSide, height: glyphSide),
                    color: TalkdeskColors.actionPrimary.mix(with: TalkdeskColors.textOnBrand, by: p).opacity(markAlpha),
                    dotAlpha: dotAlpha,
                    dotLift: dotLift,
                    dotRadius: reduce ? AppMarkGlyph.dotRadiusStatic : lerp(AppMarkGlyph.dotRadiusSplash, AppMarkGlyph.dotRadiusStatic, p)
                )
            }

            // Name sits 24pt under the 96pt mark, which is centred on the screen.
            Text("Talkdesk Conversations")
                .talkdeskText(TalkdeskTypography.title)
                .foregroundStyle(TalkdeskColors.textOnBrand)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                // The launch frame has no name; with Reduce Motion showing it only for the 200ms fade would flash.
                .opacity(reduce ? 0 : clock.nameIn * (1 - clock.nameOut))
                .offset(y: reduce ? 0 : TalkdeskSpacing.s8 * (1 - clock.nameIn))
                .padding(.top, size.height / 2 + TalkdeskSizes.avatarLarge / 2 + TalkdeskSpacing.s24)
        }
        .frame(width: size.width, height: size.height, alignment: .top)
        .opacity(reduce ? clock.reducedOverlayAlpha : 1)
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}

/// The splash timeline. Every value is derived from `elapsed` (ms since the first frame).
struct SplashClock {
    let elapsed: Double
    /// When the transition became ready (ms), or nil.
    let readyAt: Double?
    let reduceMotion: Bool

    static let dotsStart: Double = 300
    static let pulse: Double = 300
    static let stagger: Double = 150
    static let minPasses = 2
    static let nameInStart: Double = 400
    static let nameIn: Double = 300
    static let nameOut: Double = 300
    static let handoff: Double = 500
    static let loginDelay: Double = 300
    static let loginIn: Double = 300
    static let reducedFade: Double = 200
    /// Dot opacity between pulses.
    static let dotRestAlpha: Double = 0.35
    /// How far a dot rises at the top of a pulse, in glyph grid units.
    static let dotLift: CGFloat = 1.5

    // CSS easings from the splash board.
    private static let easeInOut = UnitCurve.bezier(startControlPoint: UnitPoint(x: 0.42, y: 0), endControlPoint: UnitPoint(x: 0.58, y: 1))
    private static let easeOut = UnitCurve.bezier(startControlPoint: UnitPoint(x: 0, y: 0), endControlPoint: UnitPoint(x: 0.58, y: 1))
    private static let ease = UnitCurve.bezier(startControlPoint: UnitPoint(x: 0.25, y: 0.1), endControlPoint: UnitPoint(x: 0.25, y: 1))

    /// Start of the handoff: after the dot pass that is running when the app is ready (min. two passes).
    var handoffStart: Double? {
        guard let readyAt else { return nil }
        let running = readyAt < Self.dotsStart ? 0 : Int((readyAt - Self.dotsStart) / Self.pulse)
        let passes = max(Self.minPasses, running + 1)
        return Self.dotsStart + Self.pulse * Double(passes) + Self.stagger * 2
    }

    var endTime: Double? {
        reduceMotion ? readyAt.map { $0 + Self.reducedFade } : handoffStart.map { $0 + Self.loginDelay + Self.loginIn }
    }

    var handoffProgress: Double { progress(from: handoffStart, duration: Self.handoff, curve: Self.easeInOut) }
    var contentProgress: Double { progress(from: handoffStart.map { $0 + Self.loginDelay }, duration: Self.loginIn, curve: Self.easeOut) }
    var nameIn: Double { progress(from: Self.nameInStart, duration: Self.nameIn, curve: Self.easeOut) }
    var nameOut: Double { progress(from: handoffStart, duration: Self.nameOut, curve: Self.easeOut) }
    var reducedOverlayAlpha: Double { 1 - progress(from: readyAt, duration: Self.reducedFade, curve: Self.ease) }

    /// 0–1 lift of dot `index`: each pulse rises for 150ms and falls for 150ms (ease-in-out). Dots start
    /// 150ms apart and stop after their last pass before the handoff.
    func dotPulse(_ index: Int) -> Double {
        let local = elapsed - Self.dotsStart - Self.stagger * Double(index)
        guard local >= 0 else { return 0 }
        let pass = Int(local / Self.pulse)
        if let handoffStart, pass >= Int((handoffStart - Self.dotsStart - Self.stagger * 2) / Self.pulse) { return 0 }
        let f = local.truncatingRemainder(dividingBy: Self.pulse) / Self.pulse
        return f < 0.5 ? Self.easeInOut.value(at: f * 2) : 1 - Self.easeInOut.value(at: (f - 0.5) * 2)
    }

    private func progress(from start: Double?, duration: Double, curve: UnitCurve) -> Double {
        guard let start else { return 0 }
        return curve.value(at: min(max((elapsed - start) / duration, 0), 1))
    }
}

private func lerp(_ a: CGFloat, _ b: CGFloat, _ t: Double) -> CGFloat { a + (b - a) * t }

private func lerp(_ a: CGPoint, _ b: CGPoint, _ t: Double) -> CGPoint {
    CGPoint(x: lerp(a.x, b.x, t), y: lerp(a.y, b.y, t))
}

#Preview("Splash → login · Light") {
    @Previewable @State var accountName = ""
    SplashTransition(isReady: true) {
        AccountNameView(accountName: $accountName, showsHelpLink: false, onContinue: {}, onHelp: {})
    }
    .talkdeskTheme()
}

#Preview("Splash → login · Dark") {
    @Previewable @State var accountName = ""
    SplashTransition(isReady: true) {
        AccountNameView(accountName: $accountName, showsHelpLink: false, onContinue: {}, onHelp: {})
    }
    .talkdeskTheme()
    .preferredColorScheme(.dark)
}
