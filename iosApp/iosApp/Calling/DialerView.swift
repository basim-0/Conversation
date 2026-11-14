import ConversationKit
import SwiftUI

/// "New call": the dialer as a page sheet over the app. Close (glass X) with a discard prompt when a number was typed,
/// the caller ID line, country + number readout + delete, the shared dial pad and a 72pt Call button. Renders the
/// shared `DialerViewModel` state and sends its intents, like Android's `DialerScreen`.
struct DialerView: View {
    @StateObject private var model: DialerModel
    @State private var confirmsDiscard = false
    let onClose: () -> Void

    init(onCallStarted: @escaping () -> Void, onClose: @escaping () -> Void) {
        self.onClose = onClose
        _model = StateObject(wrappedValue: DialerModel(onCallStarted: onCallStarted, onClose: onClose))
    }

    var body: some View {
        let state = model.state
        NavigationStack {
            VStack(spacing: 0) {
                callerIdLine(state)
                numberRow(state)
                if state.isNumberInvalid {
                    Text("Check the number and try again")
                        .talkdeskText(TalkdeskTypography.caption)
                        .foregroundStyle(TalkdeskColors.textError)
                        .padding(.top, TalkdeskSpacing.s4)
                }
                Spacer(minLength: TalkdeskSpacing.s16)
                TalkdeskDialpad(onKey: model.press)
                    .padding(.horizontal, TalkdeskSpacing.s24)
                callButton(state)
                    .padding(.vertical, TalkdeskSpacing.s24)
            }
            .background(TalkdeskColors.bgWindow)
            .navigationTitle("New call")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        if state.digits.isEmpty { onClose() } else { confirmsDiscard = true }
                    } label: {
                        TalkdeskIcons.x
                            .resizable()
                            .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                    }
                    .accessibilityLabel("Close")
                }
            }
            .navigationDestination(isPresented: $model.showsCountryPicker) {
                CountryPickerView(onClose: { model.showsCountryPicker = false })
            }
        }
        // Swiping the sheet away would lose the typed number silently: ask through Close instead.
        .interactiveDismissDisabled(!state.digits.isEmpty)
        .confirmationDialog("Discard this number?", isPresented: $confirmsDiscard, titleVisibility: .visible) {
            Button("Discard", role: .destructive, action: onClose)
            Button("Keep editing", role: .cancel) {}
        }
        .talkdeskToast($model.toast)
        .task { await model.observeState() }
        .task { await model.observeEffects() }
    }

    // MARK: Parts

    private func callerIdLine(_ state: DialerState) -> some View {
        HStack(spacing: TalkdeskSpacing.s8) {
            TalkdeskIcons.phoneOut
                .resizable()
                .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
            if let callerId = state.callerId {
                if let label = callerId.label, !label.isEmpty {
                    Text("Calling from \(label) · \(callerId.number)")
                } else {
                    Text("Calling from \(callerId.number)")
                }
            }
        }
        .talkdeskText(TalkdeskTypography.labelRegular)
        .foregroundStyle(TalkdeskColors.textDescription)
        .lineLimit(1)
        .frame(minHeight: 40)
        .padding(.horizontal, TalkdeskSpacing.s16)
        .opacity(state.callerId == nil ? 0 : 1)
    }

    private func numberRow(_ state: DialerState) -> some View {
        HStack(spacing: TalkdeskSpacing.s8) {
            Button(action: model.pickCountry) {
                HStack(spacing: TalkdeskSpacing.s4) {
                    Text(flag(forRegion: state.country.isoCode))
                    Text(state.country.dialCode)
                        .talkdeskText(TalkdeskTypography.bodyStrong)
                        .foregroundStyle(TalkdeskColors.textDefault)
                    TalkdeskIcons.chevDown
                        .resizable()
                        .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                        .foregroundStyle(TalkdeskColors.iconMuted)
                }
                .frame(minHeight: TalkdeskSizes.touch)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .disabled(!state.isCountrySelectionEnabled)
            .opacity(state.isCountrySelectionEnabled ? 1 : 0.38)
            .accessibilityLabel("Country: \(state.country.name), \(state.country.dialCode)")

            readout(state)

            if state.digits.isEmpty {
                Button(action: model.pickContact) {
                    TalkdeskIcons.contacts
                        .resizable()
                        .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                        .foregroundStyle(TalkdeskColors.iconDefault)
                        .frame(width: TalkdeskSizes.touch, height: TalkdeskSizes.touch)
                }
                .accessibilityLabel("Choose a contact")
            } else {
                // Delete digit; a long press clears the whole number.
                TalkdeskIcons.backspace
                    .resizable()
                    .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                    .foregroundStyle(TalkdeskColors.iconDefault)
                    .frame(width: TalkdeskSizes.touch, height: TalkdeskSizes.touch)
                    .contentShape(Rectangle())
                    .onTapGesture { model.deleteDigit() }
                    .onLongPressGesture {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        model.clearNumber()
                    }
                    .accessibilityElement(children: .ignore)
                    .accessibilityLabel("Delete digit")
                    .accessibilityAddTraits(.isButton)
                    .accessibilityAction { model.deleteDigit() }
                    .accessibilityAction(named: Text("Clear number")) { model.clearNumber() }
            }
        }
        .padding(.horizontal, TalkdeskSpacing.s16)
    }

    private func readout(_ state: DialerState) -> some View {
        Group {
            if state.digits.isEmpty {
                Text("Enter a number")
                    .foregroundStyle(TalkdeskColors.textPlaceholder)
            } else {
                Text(state.formattedNumber)
                    .foregroundStyle(TalkdeskColors.textDefault)
            }
        }
        .talkdeskText(TalkdeskTypography.numberDisplay)
        .lineLimit(1)
        .minimumScaleFactor(0.5)
        .frame(maxWidth: .infinity, minHeight: TalkdeskSizes.touch)
        // Long-press the readout to paste.
        .contextMenu {
            Button {
                if let text = UIPasteboard.general.string { model.pasteNumber(text) }
            } label: {
                Label { Text("Paste") } icon: { TalkdeskIcons.copy }
            }
        }
        .accessibilityLabel(state.digits.isEmpty ? Text("Enter a number") : Text(state.formattedNumber))
    }

    private func callButton(_ state: DialerState) -> some View {
        TalkdeskCallButton(
            .call,
            accessibilityLabel: state.digits.isEmpty ? "Call" : "Call \(state.formattedNumber)",
            size: 72
        ) {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            Task { await model.call() }
        }
        .disabled(!state.canCall)
    }
}
