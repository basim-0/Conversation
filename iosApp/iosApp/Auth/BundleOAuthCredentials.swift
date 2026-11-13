import ConversationKit
import Foundation

/// Regional OAuth clients (production) from `Info.plist`, filled at build time from the
/// git-ignored `Configuration/Secrets.xcconfig` (see `Configuration/Secrets.xcconfig.template`).
/// A region without values yields empty credentials; the shared auth logic then fails sign-in cleanly.
final class BundleOAuthCredentials: NSObject, OAuthCredentialsProvider {
    func forRegion(region: Region) -> OAuthCredentials {
        let code = Self.code(for: region)
        return OAuthCredentials(
            clientId: value("OAUTH_\(code)_CLIENT_ID"),
            privateKey: value("OAUTH_\(code)_PRIVATE_KEY"),
            keyId: value("OAUTH_\(code)_KEY_ID")
        )
    }

    private func value(_ key: String) -> String {
        (Bundle.main.object(forInfoDictionaryKey: key) as? String) ?? ""
    }

    private static func code(for region: Region) -> String {
        switch region {
        case .us: "US"
        case .ca: "CA"
        case .eu: "EU"
        case .au: "AU"
        case .uk: "UK"
        }
    }
}
