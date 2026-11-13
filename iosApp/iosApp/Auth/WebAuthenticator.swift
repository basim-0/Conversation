import AuthenticationServices
import UIKit

enum WebAuthenticatorError: Error {
    case couldNotStart
    case noCallback
}

/// The system browser step of the login (counterpart of Android's Auth Tab):
/// `ASWebAuthenticationSession` opens the sign-in page and returns the redirect URL for the
/// callback scheme, or fails when the user cancels/closes the sheet.
final class WebAuthenticator: NSObject, ASWebAuthenticationPresentationContextProviding {
    private var session: ASWebAuthenticationSession?

    func start(url: URL, callbackScheme: String, completion: @escaping (Result<URL, Error>) -> Void) {
        session?.cancel()

        let session = ASWebAuthenticationSession(url: url, callbackURLScheme: callbackScheme) { [weak self] callbackURL, error in
            self?.session = nil
            if let callbackURL {
                completion(.success(callbackURL))
            } else {
                completion(.failure(error ?? WebAuthenticatorError.noCallback))
            }
        }
        session.presentationContextProvider = self
        // Same as Android: share the browser's cookies so company SSO can reuse an existing session.
        session.prefersEphemeralWebBrowserSession = false
        self.session = session

        if !session.start() {
            self.session = nil
            completion(.failure(WebAuthenticatorError.couldNotStart))
        }
    }

    func cancel() {
        session?.cancel()
        session = nil
    }

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow) ?? ASPresentationAnchor()
    }
}
