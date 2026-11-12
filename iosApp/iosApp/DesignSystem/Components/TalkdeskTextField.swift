import SwiftUI

/// Labelled single-line input (`ta-field`). The label sits above a 48pt box (padding 12, radius 4,
/// 1pt `border-control`). Focused: 2pt `border-focus`. Error: 2pt `border-error` and the helper in
/// `text-error` with an alert icon — error text should say what to do, not just what failed.
/// Disabled (`.disabled(true)`): `surface-sunken` fill, `text-disabled`.
///
/// Keyboard and content type modifiers (`.keyboardType`, `.textContentType`, `.submitLabel`) can be
/// applied to the field from outside.
struct TalkdeskTextField<Leading: View, Trailing: View>: View {
    let label: LocalizedStringKey
    @Binding var text: String
    var placeholder: LocalizedStringKey?
    var helperText: LocalizedStringKey?
    var errorText: LocalizedStringKey?
    var isRequired: Bool
    var isSecure: Bool
    @ViewBuilder var leading: () -> Leading
    @ViewBuilder var trailing: () -> Trailing

    @FocusState private var isFocused: Bool
    @Environment(\.isEnabled) private var isEnabled

    init(
        _ label: LocalizedStringKey,
        text: Binding<String>,
        placeholder: LocalizedStringKey? = nil,
        helperText: LocalizedStringKey? = nil,
        errorText: LocalizedStringKey? = nil,
        isRequired: Bool = false,
        isSecure: Bool = false,
        @ViewBuilder leading: @escaping () -> Leading = { EmptyView() },
        @ViewBuilder trailing: @escaping () -> Trailing = { EmptyView() }
    ) {
        self.label = label
        self._text = text
        self.placeholder = placeholder
        self.helperText = helperText
        self.errorText = errorText
        self.isRequired = isRequired
        self.isSecure = isSecure
        self.leading = leading
        self.trailing = trailing
    }

    private var isError: Bool { errorText != nil }

    private var border: (width: CGFloat, color: Color) {
        if !isEnabled { return (1, TalkdeskColors.borderDivider) }
        if isError { return (2, TalkdeskColors.borderError) }
        if isFocused { return (2, TalkdeskColors.borderFocus) }
        return (1, TalkdeskColors.borderControl)
    }

    var body: some View {
        let shape = RoundedRectangle(cornerRadius: TalkdeskRadius.sm)
        VStack(alignment: .leading, spacing: TalkdeskSpacing.s4) {
            (Text(label) + (isRequired ? Text(verbatim: "\u{2009}*").foregroundColor(TalkdeskColors.textError) : Text(verbatim: "")))
                .talkdeskText(TalkdeskTypography.label)
                .foregroundStyle(isEnabled ? TalkdeskColors.textDefault : TalkdeskColors.textDisabled)

            HStack(spacing: TalkdeskSpacing.s8) {
                leading().foregroundStyle(TalkdeskColors.iconMuted)
                field
                    .talkdeskText(TalkdeskTypography.body)
                    .foregroundStyle(isEnabled ? TalkdeskColors.textDefault : TalkdeskColors.textDisabled)
                    .tint(TalkdeskColors.borderFocus)
                    .focused($isFocused)
                trailing().foregroundStyle(TalkdeskColors.iconMuted)
            }
            .padding(.horizontal, TalkdeskSpacing.s12)
            .frame(minHeight: TalkdeskSizes.input)
            .background(isEnabled ? TalkdeskColors.surface1 : TalkdeskColors.surfaceSunken, in: shape)
            .overlay { shape.strokeBorder(border.color, lineWidth: border.width) }
            .contentShape(shape)
            .onTapGesture { isFocused = true }

            if let help = errorText ?? helperText {
                HStack(spacing: TalkdeskSpacing.s4) {
                    if isError {
                        TalkdeskIcons.alert.resizable().frame(width: 16, height: 16)
                    }
                    Text(help).talkdeskText(TalkdeskTypography.caption)
                }
                .foregroundStyle(isError ? TalkdeskColors.textError : TalkdeskColors.textDescription)
            }
        }
        .accessibilityElement(children: .contain)
    }

    @ViewBuilder
    private var field: some View {
        let prompt = placeholder.map { Text($0).foregroundStyle(TalkdeskColors.textPlaceholder) }
        if isSecure {
            SecureField("", text: $text, prompt: prompt)
                .accessibilityLabel(Text(label))
        } else {
            TextField("", text: $text, prompt: prompt)
                .accessibilityLabel(Text(label))
        }
    }
}

#Preview("Text fields") {
    @Previewable @State var email = ""
    VStack(spacing: 16) {
        TalkdeskTextField("Email", text: $email, placeholder: "name@company.com",
                          helperText: "Use your Talkdesk account email", isRequired: true)
        TalkdeskTextField("Password", text: .constant("secret"), isSecure: true) {
            EmptyView()
        } trailing: {
            TalkdeskIcons.lock.resizable().frame(width: 20, height: 20)
        }
        TalkdeskTextField("Account", text: .constant("acme-support"), errorText: "We couldn't find this account")
        TalkdeskTextField("Extension", text: .constant("204")).disabled(true)
    }
    .padding()
    .talkdeskTheme()
}
