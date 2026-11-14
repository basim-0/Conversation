import ConversationKit
import Foundation

/// Phone number rules on iOS. Android uses libphonenumber; there is no equivalent in the iOS SDK and no dependency was
/// approved for it, so this applies the ITU rules that matter for dialling:
/// - every region's international dial code comes from a built-in table (names are localised by the system);
/// - a number is valid when country code + national digits are 8 to 15 digits (NANP regions need exactly 10);
/// - NANP numbers are formatted as `(555) 014-2000`, everything else in groups.
///
/// TODO: swap for libphonenumber-equivalent (e.g. PhoneNumberKit) if per-country validity and formatting matter.
final class IosPhoneNumberParser: NSObject, PhoneNumberParser {
    /// Regions that share country code 1 (the North American Numbering Plan).
    private static let nanpRegions: Set<String> = [
        "US", "CA", "AG", "AI", "AS", "BB", "BM", "BS", "DM", "DO", "GD", "GU", "JM", "KN", "KY", "LC", "MP", "MS", "PR",
        "SX", "TC", "TT", "VC", "VG", "VI",
    ]

    /// ISO 3166 alpha-2 region -> international dial code (without "+").
    private static let dialCodes: [String: String] = {
        var table: [String: String] = [:]
        let entries = """
        AD:376 AE:971 AF:93 AL:355 AM:374 AO:244 AR:54 AT:43 AU:61 AW:297 AX:358 AZ:994 BA:387 BD:880 BE:32 BF:226 \
        BG:359 BH:973 BI:257 BJ:229 BL:590 BN:673 BO:591 BQ:599 BR:55 BT:975 BW:267 BY:375 BZ:501 CC:61 CD:243 CF:236 \
        CG:242 CH:41 CI:225 CK:682 CL:56 CM:237 CN:86 CO:57 CR:506 CU:53 CV:238 CW:599 CX:61 CY:357 CZ:420 DE:49 \
        DJ:253 DK:45 DZ:213 EC:593 EE:372 EG:20 EH:212 ER:291 ES:34 ET:251 FI:358 FJ:679 FK:500 FM:691 FO:298 FR:33 \
        GA:241 GB:44 GE:995 GF:594 GG:44 GH:233 GI:350 GL:299 GM:220 GN:224 GP:590 GQ:240 GR:30 GT:502 GW:245 GY:592 \
        HK:852 HN:504 HR:385 HT:509 HU:36 ID:62 IE:353 IL:972 IM:44 IN:91 IO:246 IQ:964 IR:98 IS:354 IT:39 JE:44 \
        JO:962 JP:81 KE:254 KG:996 KH:855 KI:686 KM:269 KP:850 KR:82 KW:965 KZ:7 LA:856 LB:961 LI:423 LK:94 LR:231 \
        LS:266 LT:370 LU:352 LV:371 LY:218 MA:212 MC:377 MD:373 ME:382 MF:590 MG:261 MH:692 MK:389 ML:223 MM:95 MN:976 \
        MO:853 MQ:596 MR:222 MT:356 MU:230 MV:960 MW:265 MX:52 MY:60 MZ:258 NA:264 NC:687 NE:227 NF:672 NG:234 NI:505 \
        NL:31 NO:47 NP:977 NR:674 NU:683 NZ:64 OM:968 PA:507 PE:51 PF:689 PG:675 PH:63 PK:92 PL:48 PM:508 PS:970 \
        PT:351 PW:680 PY:595 QA:974 RE:262 RO:40 RS:381 RU:7 RW:250 SA:966 SB:677 SC:248 SD:249 SE:46 SG:65 SH:290 \
        SI:386 SJ:47 SK:421 SL:232 SM:378 SN:221 SO:252 SR:597 SS:211 ST:239 SV:503 SY:963 SZ:268 TD:235 TG:228 TH:66 \
        TJ:992 TK:690 TL:670 TM:993 TN:216 TO:676 TR:90 TV:688 TW:886 TZ:255 UA:380 UG:256 UY:598 UZ:998 VA:39 VE:58 \
        VN:84 VU:678 WF:681 WS:685 XK:383 YE:967 YT:262 ZA:27 ZM:260 ZW:263
        """
        for entry in entries.split(whereSeparator: { $0 == " " || $0 == "\n" }) {
            let parts = entry.split(separator: ":")
            if parts.count == 2 { table[String(parts[0])] = String(parts[1]) }
        }
        for region in nanpRegions { table[region] = "1" }
        return table
    }()

    /// Dial codes longest first, so `+351` is matched before `+35`.
    private static let codesByLength: [(code: String, region: String)] = {
        var preferred: [String: String] = [:]
        // Regions that own a code shared with others (the main one wins for detection).
        let owners = ["1": "US", "7": "RU", "44": "GB", "358": "FI", "39": "IT", "61": "AU", "47": "NO", "212": "MA",
                      "262": "RE", "590": "GP", "599": "CW"]
        for (region, code) in dialCodes where preferred[code] == nil || owners[code] == region {
            preferred[code] = region
        }
        return preferred.map { (code: $0.key, region: $0.value) }.sorted { $0.code.count > $1.code.count }
    }()

    // MARK: PhoneNumberParser

    func countries() -> [DialCountry] {
        let locale = Locale(identifier: "en_US")
        return Self.dialCodes.compactMap { region, code in
            guard let name = locale.localizedString(forRegionCode: region) else { return nil }
            return DialCountry(isoCode: region, name: name, dialCode: "+\(code)")
        }
    }

    func regionOf(number: String) -> String? {
        guard number.hasPrefix("+") else { return nil }
        let digits = number.dropFirst().filter(\.isNumber)
        return Self.codesByLength.first { digits.hasPrefix($0.code) }?.region
    }

    func toE164(number: String, regionIso: String) -> String? {
        let international = number.hasPrefix("+")
        let digits = number.filter(\.isNumber)
        guard !digits.isEmpty else { return nil }
        let full: String
        let region: String
        if international {
            full = digits
            region = regionOf(number: number) ?? regionIso
        } else {
            guard let code = Self.dialCodes[regionIso] else { return nil }
            region = regionIso
            // National form: drop the trunk prefix (leading 0, or 1 in NANP) before adding the country code.
            var national = Substring(digits)
            if Self.nanpRegions.contains(region) {
                if national.hasPrefix("1"), national.count == 11 { national = national.dropFirst() }
            } else if national.hasPrefix("0") {
                national = national.dropFirst()
            }
            full = code + national
        }
        let code = Self.dialCodes[region] ?? ""
        let nationalCount = full.count - code.count
        if Self.nanpRegions.contains(region) {
            guard full.hasPrefix("1"), nationalCount == 10 else { return nil }
        } else {
            guard (8...15).contains(full.count), nationalCount >= 4 else { return nil }
        }
        return "+" + full
    }

    func formatPartial(number: String, regionIso: String) -> String {
        let international = number.hasPrefix("+")
        var digits = number.filter(\.isNumber)
        let code = Self.dialCodes[international ? (regionOf(number: number) ?? regionIso) : regionIso] ?? ""
        let nanp = Self.nanpRegions.contains(international ? (regionOf(number: number) ?? regionIso) : regionIso)
        var prefix = ""
        if international {
            guard digits.hasPrefix(code), !code.isEmpty else { return number }
            digits.removeFirst(code.count)
            prefix = "+\(code) "
        }
        if nanp {
            if !international, digits.hasPrefix("1"), digits.count > 10 { digits.removeFirst() }
            return prefix + Self.formatNanp(digits)
        }
        return prefix + Self.group(digits)
    }

    private static func formatNanp(_ digits: String) -> String {
        let d = Array(digits.prefix(10))
        switch d.count {
        case 0: return ""
        case 1...3: return "(" + String(d)
        case 4...6: return "(" + String(d[0..<3]) + ") " + String(d[3...])
        default: return "(" + String(d[0..<3]) + ") " + String(d[3..<6]) + "-" + String(d[6...])
        }
    }

    /// Groups of 3 then 4 digits (`123 456 7890`), a readable default where no national format is known.
    private static func group(_ digits: String) -> String {
        var out: [String] = []
        var rest = Substring(digits)
        while rest.count > 4 {
            out.append(String(rest.prefix(3)))
            rest = rest.dropFirst(3)
        }
        if !rest.isEmpty { out.append(String(rest)) }
        return out.joined(separator: " ")
    }
}
