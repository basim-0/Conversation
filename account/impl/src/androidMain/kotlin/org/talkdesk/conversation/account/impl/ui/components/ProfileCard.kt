package org.talkdesk.conversation.account.impl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.account.impl.R
import org.talkdesk.conversation.account.impl.presentation.AccountMock
import org.talkdesk.conversation.account.impl.presentation.model.AgentProfile
import org.talkdesk.conversation.account.impl.presentation.model.PresenceKind
import org.talkdesk.conversation.account.impl.ui.AccountPreviews
import org.talkdesk.conversation.account.impl.ui.PreviewSurface
import org.talkdesk.conversation.core.designsystem.component.TalkdeskCard
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Profile card: 56dp initials avatar with the presence badge of the current status, name and
 * "role · team". The 56dp avatar is not a design-system size (it has 40dp and 96dp), so it is drawn
 * here, as on the account board.
 */
@Composable
internal fun ProfileCard(profile: AgentProfile, presence: PresenceKind?, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    TalkdeskCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(56.dp)) {
                Box(
                    Modifier.size(56.dp).background(colors.surfaceBrandSubtle, TalkdeskTheme.shapes.full),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(profile.initials, style = TalkdeskTheme.typography.title, color = colors.textBrand)
                }
                if (presence != null) {
                    val description = stringResource(presence.labelRes())
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 1.dp, y = 1.dp)
                            .size(16.dp)
                            .background(colors.surface1, TalkdeskTheme.shapes.full)
                            .semantics { contentDescription = description },
                        contentAlignment = Alignment.Center,
                    ) {
                        StatusIndicator(presence)
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    profile.name,
                    style = TalkdeskTheme.typography.title,
                    color = colors.textDefault,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(profile.subtitle, style = TalkdeskTheme.typography.labelRegular, color = colors.textDescription)
            }
        }
    }
}

private fun PresenceKind.labelRes() = when (this) {
    PresenceKind.Available -> R.string.account_presence_available
    PresenceKind.Away -> R.string.account_presence_away
    PresenceKind.Offline -> R.string.account_presence_offline
    PresenceKind.Busy -> R.string.account_presence_busy
}

@AccountPreviews
@Composable
private fun ProfileCardPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileCard(AccountMock.profile, PresenceKind.Available)
        ProfileCard(AccountMock.profile, PresenceKind.Away)
        ProfileCard(AccountMock.profile, PresenceKind.Offline)
    }
}
