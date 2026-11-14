import ConversationKit
import SwiftUI

/// Account tab: the agent's profile card with the current presence, the status list grouped by presence type as
/// inset grouped lists with trailing checkmarks, then Log out and the app version. Renders the shared
/// `AccountViewModel` state and sends its intents, like Android's `AccountScreen`.
///
/// iOS feedback habits (from the design): inline progress and haptics instead of snackbars, an inline banner with
/// Retry when a change fails, and a confirmation sheet for Log out.
struct AccountView: View {
    @StateObject private var model: AccountModel

    init(onSignOut: @escaping () -> Void) {
        _model = StateObject(wrappedValue: AccountModel(onSignOut: onSignOut))
    }

    var body: some View {
        let state = model.state
        ScrollView {
            VStack(spacing: 0) {
                header
                profileCard(state)
                if model.statusChangeFailed { errorBanner(state) }
                if state.isLoading {
                    skeleton
                } else {
                    ForEach(state.statusGroups, id: \.kind) { group in
                        groupView(group, state: state)
                    }
                }
                logOutCard
                versionBlock
            }
            .padding(.bottom, TalkdeskSpacing.s24)
        }
        .background(TalkdeskColors.bgSubtle.ignoresSafeArea())
        .sensoryFeedback(.selection, trigger: model.confirmedChanges)
        .sensoryFeedback(.error, trigger: model.failures)
        .animation(.easeOut(duration: 0.15), value: state.selectedStatusId)
        .confirmationDialog(
            "Log out",
            isPresented: Binding(
                get: { if case .confirmLogOut = onEnum(of: state.dialog) { true } else { false } },
                set: { if !$0 { model.dismissDialog() } }
            ),
            titleVisibility: .hidden
        ) {
            Button("Log out", role: .destructive, action: model.confirmLogOut)
            Button("Cancel", role: .cancel, action: model.dismissDialog)
        } message: {
            Text("You'll stop receiving calls and messages on this device until you sign in again.")
        }
        .talkdeskDialog(
            isPresented: Binding(
                get: { if case .endCallFirst = onEnum(of: state.dialog) { true } else { false } },
                set: { if !$0 { model.dismissDialog() } }
            ),
            title: "End your call before you log out"
        ) {
            TalkdeskButton("OK", variant: .ghost, action: model.dismissDialog)
        }
        .task { await model.observeState() }
        .task { await model.observeEffects() }
    }

    // MARK: Header

    /// Glass feedback button row (52pt) and the 28/34 bold title, as on the board.
    private var header: some View {
        VStack(spacing: 0) {
            HStack {
                Spacer()
                Button(action: model.sendFeedback) {
                    // The design system icon set has no feedback glyph; Message is the closest.
                    TalkdeskIcons.message
                        .resizable()
                        .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                        .foregroundStyle(TalkdeskColors.iconDefault)
                        .frame(width: 44, height: 44)
                        .talkdeskGlass(in: Circle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Send feedback")
            }
            .frame(height: 52)
            .padding(.horizontal, TalkdeskSpacing.s16)
            Text("Account")
                .talkdeskText(TalkdeskTextStyle(size: 28, lineHeight: 34, weight: .bold, tracking: 0, relativeTo: .title1))
                .foregroundStyle(TalkdeskColors.textDefault)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.init(top: 4, leading: 16, bottom: 12, trailing: 16))
                .accessibilityAddTraits(.isHeader)
        }
    }

    // MARK: Profile

    private func profileCard(_ state: AccountState) -> some View {
        HStack(spacing: 14) {
            ZStack(alignment: .bottomTrailing) {
                ZStack {
                    Circle().fill(TalkdeskColors.surfaceBrandSubtle)
                    Text(state.profile.initials)
                        .talkdeskText(TalkdeskTextStyle(size: 20, lineHeight: 25, weight: .semibold, tracking: 0, relativeTo: .title3))
                        .foregroundStyle(TalkdeskColors.textBrand)
                }
                .frame(width: 56, height: 56)
                if let presence = state.currentPresence {
                    StatusIndicator(presence: presence)
                        .frame(width: 16, height: 16)
                        .background(TalkdeskColors.surface1, in: Circle())
                        .offset(x: 1, y: 1)
                        .accessibilityLabel(presence.spokenName)
                }
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(state.profile.name)
                    .talkdeskText(TalkdeskTextStyle(size: 20, lineHeight: 25, weight: .semibold, tracking: 0, relativeTo: .title3))
                    .foregroundStyle(TalkdeskColors.textDefault)
                    .lineLimit(1)
                Text(state.profile.subtitle)
                    .talkdeskText(TalkdeskTextStyle(size: 15, lineHeight: 20, weight: .regular, tracking: 0, relativeTo: .subheadline))
                    .foregroundStyle(TalkdeskColors.textDescription)
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(TalkdeskColors.surface1, in: RoundedRectangle(cornerRadius: TalkdeskRadius.lg))
        .padding(.horizontal, 16)
        .accessibilityElement(children: .combine)
    }

    // MARK: Error banner

    /// Solid inline banner (it is content, not a control), 16 radius, with Retry at the end.
    private func errorBanner(_ state: AccountState) -> some View {
        HStack(spacing: TalkdeskSpacing.s12) {
            TalkdeskIcons.alert
                .resizable()
                .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                .foregroundStyle(TalkdeskColors.textError)
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 2) {
                Text("Couldn't change your status")
                    .talkdeskText(TalkdeskTypography.label)
                    .foregroundStyle(TalkdeskColors.textError)
                if let current = state.selectedStatus {
                    Text("You're still \(current.displayName).")
                        .talkdeskText(TalkdeskTypography.labelRegular)
                        .foregroundStyle(TalkdeskColors.textDefault)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            Button(action: model.retry) {
                Text("Retry")
                    .talkdeskText(TalkdeskTextStyle(size: 15, lineHeight: 20, weight: .semibold, tracking: 0, relativeTo: .subheadline))
                    .foregroundStyle(TalkdeskColors.textError)
                    .padding(.horizontal, 12)
                    .frame(height: 44)
            }
            .buttonStyle(.plain)
        }
        .padding(.leading, TalkdeskSpacing.s16)
        .padding(.trailing, TalkdeskSpacing.s4)
        .padding(.vertical, TalkdeskSpacing.s8)
        .background(TalkdeskColors.fillErrorSubtle, in: RoundedRectangle(cornerRadius: TalkdeskRadius.lg))
        .padding(.horizontal, 16)
        .padding(.top, 16)
        .accessibilityElement(children: .contain)
    }

    // MARK: Statuses

    private func sectionHeader(_ title: LocalizedStringKey) -> some View {
        Text(title)
            .textCase(.uppercase)
            .talkdeskText(TalkdeskTextStyle(size: 13, lineHeight: 18, weight: .medium, tracking: 0.3, relativeTo: .footnote))
            .foregroundStyle(TalkdeskColors.textDescription)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.init(top: 20, leading: 32, bottom: 6, trailing: 32))
            .accessibilityAddTraits(.isHeader)
    }

    private func groupView(_ group: StatusGroup, state: AccountState) -> some View {
        VStack(spacing: 0) {
            sectionHeader(group.kind.title)
            card {
                ForEach(Array(group.statuses.enumerated()), id: \.element.id) { index, status in
                    statusRow(status, selected: status.id == state.selectedStatusId, state: state)
                        .overlay(alignment: .bottom) {
                            if index < group.statuses.count - 1 {
                                Rectangle()
                                    .fill(TalkdeskColors.borderDivider)
                                    .frame(height: 1)
                                    .padding(.leading, 42)
                            }
                        }
                }
            }
            if group.kind == .automatic {
                Text("Starts after each call and ends on its own.")
                    .talkdeskText(TalkdeskTextStyle(size: 13, lineHeight: 18, weight: .regular, tracking: 0, relativeTo: .footnote))
                    .foregroundStyle(TalkdeskColors.textDescription)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.init(top: 6, leading: 32, bottom: 0, trailing: 32))
            }
        }
    }

    private func card<Content: View>(@ViewBuilder _ content: () -> Content) -> some View {
        VStack(spacing: 0, content: content)
            .background(TalkdeskColors.surface1, in: RoundedRectangle(cornerRadius: TalkdeskRadius.lg))
            .clipShape(RoundedRectangle(cornerRadius: TalkdeskRadius.lg))
            .padding(.horizontal, 16)
    }

    private func statusRow(_ status: AgentStatus, selected: Bool, state: AccountState) -> some View {
        // The tapped row shows a small spinner where its checkmark will land while the change is in flight.
        let busy = selected && state.isChangingStatus
        let enabled = !state.isChangingStatus && !status.isSystemSet
        return Button {
            model.select(status.id)
        } label: {
            HStack(spacing: 14) {
                StatusIndicator(presence: status.presence)
                Text(status.displayName)
                    .talkdeskText(TalkdeskTextStyle(size: 16, lineHeight: 22, weight: .regular, tracking: 0, relativeTo: .body))
                    .foregroundStyle(status.isSystemSet ? TalkdeskColors.textDescription : TalkdeskColors.textDefault)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)
                if busy {
                    ProgressView().controlSize(.small)
                } else if selected {
                    TalkdeskIcons.check
                        .resizable()
                        .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                        .foregroundStyle(TalkdeskColors.iconBrand)
                }
            }
            .padding(.horizontal, 16)
            .frame(minHeight: 48)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
        .accessibilityAddTraits(selected ? .isSelected : [])
    }

    private var skeleton: some View {
        VStack(spacing: 0) {
            sectionHeader("Available")
            card {
                ForEach(0..<3, id: \.self) { _ in
                    HStack(spacing: 14) {
                        Circle().fill(TalkdeskColors.skeletonBase).frame(width: 12, height: 12)
                        RoundedRectangle(cornerRadius: TalkdeskRadius.sm).fill(TalkdeskColors.skeletonBase).frame(width: 140, height: 12)
                        Spacer(minLength: 0)
                    }
                    .padding(.horizontal, 16)
                    .frame(minHeight: 48)
                }
            }
        }
        .accessibilityHidden(true)
    }

    // MARK: Log out

    private var logOutCard: some View {
        Button(role: .destructive, action: model.logOutClicked) {
            HStack(spacing: 14) {
                TalkdeskIcons.logout
                    .resizable()
                    .frame(width: TalkdeskSizes.icon, height: TalkdeskSizes.icon)
                Text("Log out")
                    .talkdeskText(TalkdeskTextStyle(size: 16, lineHeight: 22, weight: .regular, tracking: 0, relativeTo: .body))
                Spacer(minLength: 0)
            }
            .foregroundStyle(TalkdeskColors.textError)
            .padding(.horizontal, 16)
            .frame(minHeight: 48)
            .background(TalkdeskColors.surface1, in: RoundedRectangle(cornerRadius: TalkdeskRadius.lg))
            .contentShape(RoundedRectangle(cornerRadius: TalkdeskRadius.lg))
        }
        .buttonStyle(.plain)
        .padding(.horizontal, 16)
        .padding(.top, 24)
    }

    private var versionBlock: some View {
        VStack(spacing: 2) {
            Text("Talkdesk Conversations")
                .talkdeskText(TalkdeskTextStyle(size: 13, lineHeight: 18, weight: .semibold, tracking: 0, relativeTo: .footnote))
            Text("Version \(Self.appVersion)")
                .talkdeskText(TalkdeskTextStyle(size: 13, lineHeight: 18, weight: .regular, tracking: 0, relativeTo: .footnote))
        }
        .foregroundStyle(TalkdeskColors.textDescription)
        .frame(maxWidth: .infinity)
        .padding(.top, 24)
    }

    private static var appVersion: String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? ""
    }
}

/// 12pt presence mark of the Account design: disc (available), 3pt ring (away), 2pt ring (offline) and a rounded
/// square (busy / set by the system). Shape and colour together, never colour alone. Private to the account feature,
/// like Android's `StatusIndicator`.
private struct StatusIndicator: View {
    let presence: PresenceKind
    var size: CGFloat = 12

    var body: some View {
        Group {
            switch presence {
            case .available: Circle().fill(TalkdeskColors.presenceAvailable)
            case .away: Circle().strokeBorder(TalkdeskColors.presenceAway, lineWidth: 3)
            case .offline: Circle().strokeBorder(TalkdeskColors.presenceOffline, lineWidth: 2)
            case .busy: RoundedRectangle(cornerRadius: 3).fill(TalkdeskColors.presenceBusy)
            }
        }
        .frame(width: size, height: size)
        .accessibilityHidden(true)
    }
}
