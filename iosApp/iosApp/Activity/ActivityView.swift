import ConversationKit
import SwiftUI

/// Activity tab: calls and conversations in one timeline grouped by day. Uses native iOS controls (large title,
/// toolbar buttons, segmented picker, pull-down menu), so they get Liquid Glass on iOS 26+. Rows, banner and states
/// use the Talkdesk components. Renders the shared `ActivityViewModel` state and sends its intents, like Android's
/// `ActivityScreen`.
struct ActivityView: View {
    @StateObject private var model = ActivityModel()
    /// "New call" opens the dialer, a separate feature the host presents (like Android's `onNewCall`).
    var onNewCall: () -> Void = {}

    var body: some View {
        let state = model.state
        NavigationStack {
            screen(state)
                .background(TalkdeskColors.bgWindow)
                .navigationTitle("Activity")
                .navigationBarTitleDisplayMode(.large)
                .toolbar { toolbar(state) }
        }
        .task { await model.observeState() }
    }

    /// One `List` for every state (loading, failed, empty, rows), so the scroll view, pinned header and toolbar stay
    /// the same while the selection changes and only the rows inside change. It is also the scroll view the large title
    /// tracks (a `safeAreaInset` header breaks the title's return).
    private func screen(_ state: ActivityState) -> some View {
        // Ask for the next page when one of the last rows comes into view.
        var loadMoreIds = Set<String>()
        if case .loaded(let loaded) = onEnum(of: state.content) {
            loadMoreIds = Set(loaded.sections.flatMap(\.items).suffix(3).map(\.id))
        }
        return List {
            Section {
                rows(state, loadMoreIds: loadMoreIds)
            } header: {
                header(state).listRowInsets(EdgeInsets()).textCase(nil)
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .environment(\.defaultMinListRowHeight, 0)
        .refreshable { await model.refresh() }
        // Changing the tab or a filter swaps rows in place; no implicit animation of the whole list.
        .transaction { $0.animation = nil }
    }

    // MARK: Header

    private func header(_ state: ActivityState) -> some View {
        VStack(spacing: 0) {
            Picker("Show", selection: Binding(get: { state.selectedTab }, set: { model.selectTab($0) })) {
                Text("All").tag(ActivityTab.all)
                Text(missedTitle(state)).tag(ActivityTab.missed)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, TalkdeskSpacing.s16)
            .padding(.bottom, TalkdeskSpacing.s8)
            if canFilter(state), !state.selectedChannels.isEmpty {
                filterSummary(state)
            }
            if state.isOffline {
                TalkdeskBanner(
                    title: "You're offline",
                    message: offlineMessage(state),
                    variant: .info,
                    icon: TalkdeskIcons.wifiOff,
                    cornerRadius: 12
                )
                .padding(.horizontal, TalkdeskSpacing.s16)
                .padding(.top, TalkdeskSpacing.s8)
                .padding(.bottom, TalkdeskSpacing.s4)
            }
        }
        .background(TalkdeskColors.bgWindow)
    }

    /// "Filtered by Calls, WhatsApp" with a clear button, like Mail's "Filtered by" line. Shown only while a filter is on,
    /// so the active state is visible without opening the menu.
    private func filterSummary(_ state: ActivityState) -> some View {
        let names = ActivityChannel.filterOrder.filter { state.selectedChannels.contains($0) }.map(\.plainTitle)
        return HStack(spacing: TalkdeskSpacing.s8) {
            Text("Filtered by \(names.formatted(.list(type: .and, width: .narrow)))")
                .talkdeskText(TalkdeskTypography.rowSubtitle)
                .foregroundStyle(TalkdeskColors.textDescription)
                .lineLimit(1)
            Spacer(minLength: 0)
            Button(action: model.clearFilters) {
                HStack(spacing: TalkdeskSpacing.s4) {
                    TalkdeskIcons.x
                        .resizable()
                        .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                    Text("Clear")
                        .talkdeskText(TalkdeskTypography.rowSubtitle)
                }
            }
            .tint(TalkdeskColors.iconBrand)
        }
        .padding(.horizontal, TalkdeskSpacing.s16)
        .padding(.vertical, TalkdeskSpacing.s4)
        .accessibilityElement(children: .contain)
    }

    private func missedTitle(_ state: ActivityState) -> String {
        // The API has no unread state yet, so the count is 0 and this is just "Missed".
        if state.selectedTab != .missed, state.missedCount > 0 {
            String(localized: "Missed (\(Int(state.missedCount)))")
        } else {
            String(localized: "Missed")
        }
    }

    private func offlineMessage(_ state: ActivityState) -> LocalizedStringKey {
        if let updated = state.lastUpdatedLabel {
            "Showing activity from \(updated). It updates when you reconnect."
        } else {
            "It updates when you reconnect."
        }
    }

    // MARK: Toolbar

    private func canFilter(_ state: ActivityState) -> Bool {
        // Not tied to loading: picking a filter reloads, and the button (and its open menu) must not disappear.
        state.selectedTab == .all
    }

    private func canStartCall(_ state: ActivityState) -> Bool {
        !state.isOffline
    }

    @ToolbarContentBuilder
    private func toolbar(_ state: ActivityState) -> some ToolbarContent {
        if canFilter(state) {
            ToolbarItem(placement: .topBarTrailing) {
                filterMenu(state)
            }
        }
        if canFilter(state) && canStartCall(state), #available(iOS 26.0, *) {
            // iOS 26 groups neighbouring toolbar items into one glass pill; a fixed spacer separates them.
            ToolbarSpacer(.fixed, placement: .topBarTrailing)
        }
        if canStartCall(state) {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: onNewCall) {
                    TalkdeskIcons.phone
                        .resizable()
                        .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                }
                .tint(TalkdeskColors.iconBrand)
                .accessibilityLabel("New call")
            }
        }
    }

    /// Multi-select channel filter. Stays open while options are toggled.
    private func filterMenu(_ state: ActivityState) -> some View {
        let isActive = !state.selectedChannels.isEmpty
        return Menu {
            Section("Show") {
                ForEach(ActivityChannel.filterOrder, id: \.self) { channel in
                    let selected = state.selectedChannels.contains(channel)
                    // A Button, not a Toggle: a Toggle's system checkmark makes the menu reserve a leading column and
                    // shifts the unselected rows. Every row always has an icon, and the selected one shows a check.
                    Button { model.toggleChannel(channel) } label: {
                        Label {
                            Text(channel.title)
                        } icon: {
                            selected ? TalkdeskIcons.check : channel.icon
                        }
                    }
                    .tint(selected ? TalkdeskColors.iconBrand : nil)
                    .accessibilityAddTraits(selected ? .isSelected : [])
                }
            }
            Divider()
            Button("Clear filters", action: model.clearFilters)
                .disabled(!isActive)
        } label: {
            TalkdeskIcons.filter
                .resizable()
                .frame(width: TalkdeskSizes.iconSmall, height: TalkdeskSizes.iconSmall)
                // Colour only the icon. A `.tint` on the Menu would also recolour every item inside it.
                .foregroundStyle(isActive ? TalkdeskColors.iconBrand : TalkdeskColors.iconDefault)
                .overlay(alignment: .topTrailing) {
                    // Dot on the icon while a filter is on.
                    if isActive {
                        Circle()
                            .fill(TalkdeskColors.iconBrand)
                            .frame(width: TalkdeskSpacing.s8, height: TalkdeskSpacing.s8)
                            .offset(x: TalkdeskSpacing.s4, y: -TalkdeskSpacing.s4)
                    }
                }
        }
        .menuActionDismissBehavior(.disabled)
        .accessibilityLabel("Filter")
        .accessibilityValue(isActive ? Text("Active") : Text(verbatim: ""))
    }

    // MARK: Content

    @ViewBuilder
    private func rows(_ state: ActivityState, loadMoreIds: Set<String>) -> some View {
        switch onEnum(of: state.content) {
        case .loading:
            ActivityLoadingView().plainListRow()
        case .failed:
            ActivityMessageView(
                icon: TalkdeskIcons.wifiOff,
                title: "Couldn't load activity",
                message: "Check your connection and try again.",
                actionTitle: "Try again",
                action: model.retry
            )
            .plainListRow()
        case .loaded(let loaded):
            if loaded.sections.isEmpty {
                emptyState(state).plainListRow()
            } else {
                ForEach(Array(loaded.sections.enumerated()), id: \.offset) { _, section in
                    Text(section.day.headerLabel)
                        .talkdeskText(TalkdeskTypography.sectionHeader)
                        .foregroundStyle(TalkdeskColors.textDescription)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, TalkdeskSpacing.s16)
                        .padding(.top, TalkdeskSpacing.s12)
                        .padding(.bottom, TalkdeskSpacing.s4)
                        .accessibilityAddTraits(.isHeader)
                        .plainListRow()
                    ForEach(section.items, id: \.id) { item in
                        ActivityRowView(
                            item: item,
                            offline: state.isOffline,
                            onOpen: { model.open(item.id) },
                            onCallBack: { model.callBack(item.id) }
                        )
                        .plainListRow()
                        .onAppear { if loadMoreIds.contains(item.id) { model.loadMore() } }
                    }
                }
                if state.isLoadingMore {
                    TalkdeskSpinner()
                        .frame(maxWidth: .infinity)
                        .padding(TalkdeskSpacing.s16)
                        .plainListRow()
                }
            }
        }
    }

    @ViewBuilder
    private func emptyState(_ state: ActivityState) -> some View {
        if state.selectedTab == .missed {
            ActivityMessageView(
                icon: TalkdeskIcons.check,
                title: "No missed calls",
                message: "Calls you miss will show up here, so you can call back."
            )
        } else if let single = state.selectedChannels.count == 1 ? state.selectedChannels.first : nil {
            ActivityMessageView(
                icon: single.icon,
                title: "No \(single.plainTitle) activity",
                message: "Nothing matches your filter. Clear it to see all activity.",
                actionTitle: "Clear filters",
                action: model.clearFilters
            )
        } else if !state.selectedChannels.isEmpty {
            ActivityMessageView(
                icon: TalkdeskIcons.search,
                title: "No matching activity",
                message: "Try a different channel, or clear the filters.",
                actionTitle: "Clear filters",
                action: model.clearFilters
            )
        } else {
            ActivityMessageView(
                icon: TalkdeskIcons.clock,
                title: "No activity yet",
                message: "Calls and conversations will show up here."
            )
        }
    }
}

extension View {
    /// A list row without the system insets, separator or background, so rows draw their own.
    fileprivate func plainListRow() -> some View {
        listRowInsets(EdgeInsets())
            .listRowSeparator(.hidden)
            .listRowBackground(Color.clear)
    }
}
