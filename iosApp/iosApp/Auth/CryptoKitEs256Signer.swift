import ConversationKit
import CryptoKit
import Foundation

/// ES256 signing for the Talkdesk ID client assertion (counterpart of Android's `JavaEs256Signer`).
///
/// Takes the base64 PKCS#8 EC private key and returns the JOSE signature (raw `R || S`, 64 bytes).
/// If the key cannot be loaded or signing fails it returns an empty array: the server then rejects
/// the assertion and sign-in fails cleanly instead of crashing the app.
final class CryptoKitEs256Signer: NSObject, Es256Signer {
    func sign(privateKeyPkcs8Base64: String, payload: KotlinByteArray) -> KotlinByteArray {
        let cleaned = privateKeyPkcs8Base64.filter { !$0.isWhitespace }
        guard
            let der = Data(base64Encoded: cleaned),
            let key = try? P256.Signing.PrivateKey(derRepresentation: der),
            let signature = try? key.signature(for: payload.toData())
        else {
            return KotlinByteArray(size: 0)
        }
        return signature.rawRepresentation.toKotlinByteArray()
    }
}

extension KotlinByteArray {
    func toData() -> Data {
        var data = Data(count: Int(size))
        for index in 0..<Int(size) {
            data[index] = UInt8(bitPattern: get(index: Int32(index)))
        }
        return data
    }
}

extension Data {
    func toKotlinByteArray() -> KotlinByteArray {
        let array = KotlinByteArray(size: Int32(count))
        for (index, byte) in enumerated() {
            array.set(index: Int32(index), value: Int8(bitPattern: byte))
        }
        return array
    }
}
