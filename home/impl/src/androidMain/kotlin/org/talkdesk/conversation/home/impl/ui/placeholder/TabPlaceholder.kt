package org.talkdesk.conversation.home.impl.ui.placeholder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskButtonVariant
import org.talkdesk.conversation.core.designsystem.component.TalkdeskEmptyState
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.home.api.AccountRoute
import org.talkdesk.conversation.home.api.ContactsRoute
import org.talkdesk.conversation.home.api.ConversationsRoute
import org.talkdesk.conversation.home.api.VoicemailsRoute
import org.talkdesk.conversation.home.impl.R
import org.talkdesk.conversation.home.impl.presentation.HomeTab
import org.talkdesk.conversation.home.impl.ui.HomePreviews

/** Navigation entries for the tabs that have no feature yet. */
internal fun EntryProviderScope<NavKey>.placeholderEntries(onSignOut: () -> Unit) {
    entry<ContactsRoute> { TabPlaceholder(HomeTab.Contacts, onSignOut) }
    entry<ConversationsRoute> { TabPlaceholder(HomeTab.Conversations, onSignOut) }
    entry<VoicemailsRoute> { TabPlaceholder(HomeTab.Voicemails, onSignOut) }
    entry<AccountRoute> { TabPlaceholder(HomeTab.Account, onSignOut) }
}

/**
 * Stand-in for the tabs that are not designed yet (Contacts, Conversations, Voicemails, Account).
 * Account keeps a sign-out action so the session can still be ended.
 */
@Composable
internal fun TabPlaceholder(
    tab: HomeTab,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TalkdeskTheme.colors
    val name = stringResource(tab.labelRes())
    Column(modifier.fillMaxSize().background(colors.bgWindow).windowInsetsPadding(WindowInsets.statusBars)) {
        Box(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart) {
            Text(name, style = TalkdeskTheme.typography.h1, color = colors.textDefault, modifier = Modifier.semantics { heading() })
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TalkdeskEmptyState(
                icon = tab.icon(),
                title = stringResource(R.string.home_placeholder_title),
                body = stringResource(R.string.home_placeholder_body, name),
                action = if (tab == HomeTab.Account) {
                    {
                        TalkdeskButton(
                            text = stringResource(R.string.home_sign_out),
                            onClick = onSignOut,
                            variant = TalkdeskButtonVariant.Secondary,
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

internal fun HomeTab.labelRes(): Int = when (this) {
    HomeTab.Activity -> R.string.home_nav_activity
    HomeTab.Contacts -> R.string.home_nav_contacts
    HomeTab.Conversations -> R.string.home_nav_conversations
    HomeTab.Voicemails -> R.string.home_nav_voicemails
    HomeTab.Account -> R.string.home_nav_account
}

@Composable
internal fun HomeTab.icon(): ImageVector = when (this) {
    HomeTab.Activity -> TalkdeskIcons.Clock
    HomeTab.Contacts -> TalkdeskIcons.Contacts
    HomeTab.Conversations -> TalkdeskIcons.Message
    HomeTab.Voicemails -> TalkdeskIcons.Voicemail
    HomeTab.Account -> TalkdeskIcons.User
}

@HomePreviews
@Composable
private fun ContactsPlaceholderPreview() = TalkdeskTheme { TabPlaceholder(HomeTab.Contacts, onSignOut = {}) }

@HomePreviews
@Composable
private fun AccountPlaceholderPreview() = TalkdeskTheme { TabPlaceholder(HomeTab.Account, onSignOut = {}) }
