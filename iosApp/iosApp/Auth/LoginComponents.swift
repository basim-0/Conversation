import SwiftUI

/// UX spec: on iPad and in landscape the login form sits in a centred column, max 400pt wide.
let loginFormMaxWidth: CGFloat = 400

/// App mark: an `action-primary` rounded square (`radius-md`) with the chat-bubble glyph
/// (`AppMarkGlyph`) in `text-on-brand`. Placeholder until the official Talkdesk Conversations icon
/// exists (design open question 5).
/// `isLarge`: 56pt mark with a 32pt glyph ("Signing you in"); otherwise 40pt with a 24pt glyph.
struct AppMark: View {
    var isLarge: Bool = false

    var body: some View {
        let side = isLarge ? TalkdeskSizes.control : TalkdeskSizes.avatar
        let glyph = isLarge ? TalkdeskSpacing.s32 : TalkdeskSizes.icon
        Canvas { context, size in
            context.fill(
                RoundedRectangle(cornerRadius: TalkdeskRadius.md).path(in: CGRect(origin: .zero, size: size)),
                with: .color(TalkdeskColors.actionPrimary)
            )
            AppMarkGlyph.draw(
                in: context,
                rect: CGRect(x: (size.width - glyph) / 2, y: (size.height - glyph) / 2, width: glyph, height: glyph),
                color: TalkdeskColors.textOnBrand
            )
        }
        .frame(width: side, height: side)
        .accessibilityHidden(true)
    }
}

/// Where the login header's mark sits, so `SplashTransition` can land the splash mark on it.
struct AppMarkAnchorKey: PreferenceKey {
    static let defaultValue: Anchor<CGRect>? = nil
    static func reduce(value: inout Anchor<CGRect>?, nextValue: () -> Anchor<CGRect>?) {
        value = value ?? nextValue()
    }
}

extension EnvironmentValues {
    /// True while `SplashTransition` is drawing the mark itself.
    @Entry var appMarkHidden: Bool = false
}

/// Header of the login screens: `AppMark` followed by the app name. Inside a `SplashTransition` the
/// mark reports where it sits and stays hidden until the splash mark has landed on it.
struct AppLockup: View {
    @Environment(\.appMarkHidden) private var markHidden

    var body: some View {
        HStack(spacing: TalkdeskSpacing.s12) {
            AppMark()
                .anchorPreference(key: AppMarkAnchorKey.self, value: .bounds) { $0 }
                .opacity(markHidden ? 0 : 1)
            Text("Talkdesk Conversations")
                .talkdeskText(TalkdeskTypography.bodyStrong)
                .foregroundStyle(TalkdeskColors.textDefault)
        }
    }
}

/// Shared frame of the login form screens: `bg-window`, a scrolling column inside the safe area
/// (20pt sides, 24pt below the status bar), the `AppLockup`, `title` in `h1` and `description`.
/// `content` follows 24pt below the description.
struct LoginFormLayout<Content: View>: View {
    let title: LocalizedStringKey
    let description: LocalizedStringKey
    @ViewBuilder var content: () -> Content

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                AppLockup()
                Text(title)
                    .talkdeskText(TalkdeskTypography.h1)
                    .foregroundStyle(TalkdeskColors.textDefault)
                    .accessibilityAddTraits(.isHeader)
                    .padding(.top, TalkdeskSpacing.s32)
                    .padding(.bottom, TalkdeskSpacing.s8)
                Text(description)
                    .talkdeskText(TalkdeskTypography.body)
                    .foregroundStyle(TalkdeskColors.textDescription)
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.bottom, TalkdeskSpacing.s24)
                content()
            }
            .frame(maxWidth: loginFormMaxWidth)
            .padding(.horizontal, TalkdeskSpacing.s20)
            .padding(.top, TalkdeskSpacing.s24)
            .padding(.bottom, TalkdeskSpacing.s16)
            .frame(maxWidth: .infinity)
        }
        .scrollBounceBehavior(.basedOnSize)
        .background(TalkdeskColors.bgWindow.ignoresSafeArea())
    }
}

/// Remembered account inside a card: globe badge, account name and its web address.
struct RememberedAccountRow: View {
    let accountName: String

    var body: some View {
        HStack(spacing: TalkdeskSpacing.s12) {
            TalkdeskIcons.globe
                .resizable()
                .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                .foregroundStyle(TalkdeskColors.iconBrand)
                .frame(width: TalkdeskSizes.avatar, height: TalkdeskSizes.avatar)
                .background(TalkdeskColors.surfaceBrandSubtle, in: Circle())
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 0) {
                Text(verbatim: accountName)
                    .talkdeskText(TalkdeskTypography.bodyStrong)
                    .foregroundStyle(TalkdeskColors.textDefault)
                Text("\(accountName).mytalkdesk.com")
                    .talkdeskText(TalkdeskTypography.caption)
                    .foregroundStyle(TalkdeskColors.textDescription)
            }
        }
        .accessibilityElement(children: .combine)
    }
}
