import SwiftUI

/// "Signing you in…": shown after the sign-in sheet hands the callback back to the app, while the
/// session is created (matches Android A08).
struct SigningInView: View {
    let accountName: String

    var body: some View {
        VStack(spacing: TalkdeskSpacing.s24) {
            AppMark(isLarge: true)
            TalkdeskSpinner()
            VStack(spacing: TalkdeskSpacing.s4) {
                Text("Signing you in…")
                    .talkdeskText(TalkdeskTypography.bodyStrong)
                    .foregroundStyle(TalkdeskColors.textDefault)
                Text("\(accountName).mytalkdesk.com")
                    .talkdeskText(TalkdeskTypography.caption)
                    .foregroundStyle(TalkdeskColors.textDescription)
            }
            .accessibilityElement(children: .combine)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(TalkdeskColors.bgWindow.ignoresSafeArea())
    }
}

#Preview("Signing in · Light") {
    SigningInView(accountName: "acme").talkdeskTheme()
}

#Preview("Signing in · Dark") {
    SigningInView(accountName: "acme").talkdeskTheme().preferredColorScheme(.dark)
}
