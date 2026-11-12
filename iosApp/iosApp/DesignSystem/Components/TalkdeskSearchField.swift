import SwiftUI

/// Inline search (`ta-search`): a 40pt `surface-sunken` box with a leading search icon,
/// `text-placeholder` hint and a clear button once text is typed. Pass `onCancel` to show the
/// borderless "Cancel" action that exits search.
struct TalkdeskSearchField: View {
    @Binding var query: String
    let placeholder: LocalizedStringKey
    var clearLabel: LocalizedStringKey = "Clear"
    var cancelLabel: LocalizedStringKey = "Cancel"
    var onSubmit: (() -> Void)?
    var onCancel: (() -> Void)?

    var body: some View {
        HStack(spacing: TalkdeskSpacing.s8) {
            HStack(spacing: TalkdeskSpacing.s8) {
                TalkdeskIcons.search
                    .resizable()
                    .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                    .foregroundStyle(TalkdeskColors.iconMuted)
                TextField("", text: $query, prompt: Text(placeholder).foregroundStyle(TalkdeskColors.textPlaceholder))
                    .talkdeskText(TalkdeskTypography.body)
                    .foregroundStyle(TalkdeskColors.textDefault)
                    .tint(TalkdeskColors.borderFocus)
                    .submitLabel(.search)
                    .onSubmit { onSubmit?() }
                    .accessibilityLabel(Text(placeholder))
                if !query.isEmpty {
                    Button { query = "" } label: {
                        TalkdeskIcons.x
                            .resizable()
                            .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                            .foregroundStyle(TalkdeskColors.iconMuted)
                            .frame(width: TalkdeskSizes.touch, height: TalkdeskSizes.touch)
                            .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(Text(clearLabel))
                }
            }
            .padding(.leading, TalkdeskSpacing.s12)
            .padding(.trailing, query.isEmpty ? TalkdeskSpacing.s12 : 0)
            .frame(height: 40)
            .background(TalkdeskColors.surfaceSunken, in: RoundedRectangle(cornerRadius: TalkdeskRadius.sm))

            if let onCancel {
                TalkdeskButton(cancelLabel, variant: .ghost, action: onCancel)
            }
        }
    }
}

#Preview("Search") {
    @Previewable @State var query = ""
    VStack(spacing: 12) {
        TalkdeskSearchField(query: $query, placeholder: "Search contacts")
        TalkdeskSearchField(query: .constant("Maria"), placeholder: "Search contacts", onCancel: {})
    }
    .padding()
    .talkdeskTheme()
}
