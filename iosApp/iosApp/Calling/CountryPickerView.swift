import ConversationKit
import SwiftUI

@MainActor
final class CountryPickerModel: ObservableObject {
    @Published private(set) var state: CountryPickerState

    private let scope: ScopedViewModel<CountryPickerViewModel>
    private let onClose: () -> Void

    init(onClose: @escaping () -> Void) {
        self.onClose = onClose
        scope = ConversationBootstrap.shared.makeCountryPickerViewModel()
        state = scope.viewModel.state.value
    }

    deinit {
        scope.clear()
    }

    func observeState() async {
        for await next in scope.viewModel.state { state = next }
    }

    func observeEffects() async {
        for await effect in scope.viewModel.effects {
            switch onEnum(of: effect) {
            case .close: onClose()
            }
        }
    }

    func queryChanged(_ query: String) { scope.viewModel.onIntent(intent: CountryPickerIntentQueryChanged(query: query)) }
    func select(_ isoCode: String) { scope.viewModel.onIntent(intent: CountryPickerIntentSelect(isoCode: isoCode)) }
}

/// Country list opened from the dialer to pick the dial code: search by name or code, the picked country is checked.
/// Same state and intents as Android's `CountryPickerScreen`.
struct CountryPickerView: View {
    @StateObject private var model: CountryPickerModel
    @State private var query = ""

    init(onClose: @escaping () -> Void) {
        _model = StateObject(wrappedValue: CountryPickerModel(onClose: onClose))
    }

    var body: some View {
        let state = model.state
        VStack(spacing: 0) {
            TalkdeskSearchField(query: $query, placeholder: "Search by name or code", clearLabel: "Clear search")
                .padding(.horizontal, TalkdeskSpacing.s16)
                .padding(.vertical, TalkdeskSpacing.s8)
            if state.isEmptyResult {
                TalkdeskEmptyState(
                    icon: TalkdeskIcons.globe,
                    title: "No countries found",
                    message: "Try a different name or dial code."
                )
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                List(state.countries, id: \.isoCode) { country in
                    Button { model.select(country.isoCode) } label: {
                        HStack(spacing: TalkdeskSpacing.s12) {
                            Text(flag(forRegion: country.isoCode))
                            Text(country.name)
                                .talkdeskText(country.isoCode == state.selectedIsoCode ? TalkdeskTypography.bodyStrong : TalkdeskTypography.body)
                                .foregroundStyle(TalkdeskColors.textDefault)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            Text(country.dialCode)
                                .talkdeskText(TalkdeskTypography.labelRegular)
                                .foregroundStyle(TalkdeskColors.textDescription)
                            if country.isoCode == state.selectedIsoCode {
                                TalkdeskIcons.check
                                    .resizable()
                                    .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                                    .foregroundStyle(TalkdeskColors.iconBrand)
                            }
                        }
                        .frame(minHeight: TalkdeskSizes.touch)
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .listRowBackground(TalkdeskColors.bgWindow)
                    .accessibilityAddTraits(country.isoCode == state.selectedIsoCode ? .isSelected : [])
                }
                .listStyle(.plain)
            }
        }
        .background(TalkdeskColors.bgWindow)
        .navigationTitle("Select country")
        .navigationBarTitleDisplayMode(.inline)
        .onChange(of: query) { _, new in model.queryChanged(new) }
        .task { await model.observeState() }
        .task { await model.observeEffects() }
    }
}
