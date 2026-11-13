import SwiftUI

/// Inline message above the account field (iOS has no snackbar pattern).
enum LoginBannerStyle {
    /// Info: the sign-in sheet closed without a callback (i07).
    case signInDidNotFinish
    /// Error: SSO or session creation failed.
    case signInFailed
    /// Error: no connection before opening sign-in (i03). Continue retries.
    case offline
}

/// Login: account name (iOS board i01–i04, i07). Stateless; the caller owns every value.
///
/// - `errorText`: field error ("Enter your account name." / invalid format); replaces the helper.
/// - `isOpeningSignIn`: i04 — the button shows a spinner and "Opening sign-in…", the field
///   ignores input and the keyboard closes.
struct AccountNameView: View {
    @Binding var accountName: String
    var errorText: LocalizedStringKey?
    var banner: LoginBannerStyle?
    var isOpeningSignIn: Bool = false
    var showsHelpLink: Bool = true
    let onContinue: () -> Void
    let onHelp: () -> Void

    var body: some View {
        LoginFormLayout(
            title: "Sign in",
            description: "Enter your Talkdesk account name. You'll continue to your company's sign-in page."
        ) {
            if let banner {
                bannerView(banner)
                    .padding(.bottom, TalkdeskSpacing.s16)
            }

            TalkdeskTextField(
                "Account name",
                text: $accountName,
                placeholder: "your-company",
                helperText: "The first part of your Talkdesk web address",
                errorText: errorText
            ) {
                EmptyView()
            } trailing: {
                Text(".mytalkdesk.com")
                    .talkdeskText(TalkdeskTypography.body)
                    .foregroundStyle(TalkdeskColors.textDescription)
            }
            .keyboardType(.asciiCapable)
            .textInputAutocapitalization(.never)
            .autocorrectionDisabled()
            .submitLabel(.continue)
            .onSubmit(onContinue)
            .allowsHitTesting(!isOpeningSignIn)

            TalkdeskButton(
                isOpeningSignIn ? "Opening sign-in…" : "Continue",
                fullWidth: true,
                isLoading: isOpeningSignIn,
                action: onContinue
            )
            .padding(.top, TalkdeskSpacing.s24)

            if showsHelpLink {
                TalkdeskButton("Can't find your account name?", variant: .ghost, action: onHelp)
                    .frame(maxWidth: .infinity)
                    .padding(.top, TalkdeskSpacing.s8)
            }
        }
        .onChange(of: isOpeningSignIn) { _, opening in
            if opening {
                UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
            }
        }
    }

    @ViewBuilder
    private func bannerView(_ banner: LoginBannerStyle) -> some View {
        switch banner {
        case .signInDidNotFinish:
            TalkdeskBanner(
                title: "Sign-in didn't finish",
                message: "If you didn't see your company's sign-in page, check that \"\(accountName)\" is your account name.",
                variant: .info
            )
        case .signInFailed:
            TalkdeskBanner(
                title: "Sign-in couldn't be completed",
                message: "Try again, or contact your Talkdesk administrator.",
                variant: .error
            )
        case .offline:
            TalkdeskBanner(
                title: "No internet connection",
                message: "Check your connection, then tap Continue.",
                variant: .error,
                icon: TalkdeskIcons.wifiOff
            )
        }
    }
}

// MARK: - Previews

private struct AccountNamePreview: View {
    @State var accountName: String
    var errorText: LocalizedStringKey?
    var banner: LoginBannerStyle?
    var isOpeningSignIn = false
    var showsHelpLink = true

    var body: some View {
        AccountNameView(
            accountName: $accountName,
            errorText: errorText,
            banner: banner,
            isOpeningSignIn: isOpeningSignIn,
            showsHelpLink: showsHelpLink,
            onContinue: {},
            onHelp: {}
        )
        .talkdeskTheme()
    }
}

#Preview("01 Ready · Light") { AccountNamePreview(accountName: "", showsHelpLink: false) }
#Preview("01 Ready · Dark") { AccountNamePreview(accountName: "", showsHelpLink: false).preferredColorScheme(.dark) }

#Preview("02 Invalid · Light") {
    AccountNamePreview(accountName: "acme corp", errorText: "Use only letters, numbers and hyphens, without spaces.", showsHelpLink: false)
}
#Preview("02 Invalid · Dark") {
    AccountNamePreview(accountName: "acme corp", errorText: "Use only letters, numbers and hyphens, without spaces.", showsHelpLink: false)
        .preferredColorScheme(.dark)
}

#Preview("03 Offline · Light") { AccountNamePreview(accountName: "acme", banner: .offline) }
#Preview("03 Offline · Dark") { AccountNamePreview(accountName: "acme", banner: .offline).preferredColorScheme(.dark) }

#Preview("04 Opening sign-in · Light") { AccountNamePreview(accountName: "acme", isOpeningSignIn: true) }
#Preview("04 Opening sign-in · Dark") { AccountNamePreview(accountName: "acme", isOpeningSignIn: true).preferredColorScheme(.dark) }

#Preview("07 Didn't finish · Light") { AccountNamePreview(accountName: "acme", banner: .signInDidNotFinish) }
#Preview("07 Didn't finish · Dark") { AccountNamePreview(accountName: "acme", banner: .signInDidNotFinish).preferredColorScheme(.dark) }

#Preview("SSO error · Light") { AccountNamePreview(accountName: "acme", banner: .signInFailed) }
#Preview("SSO error · Dark") { AccountNamePreview(accountName: "acme", banner: .signInFailed).preferredColorScheme(.dark) }
