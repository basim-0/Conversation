package org.talkdesk.conversation.activity.impl.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.core.designsystem.component.TalkdeskListRow
import org.talkdesk.conversation.core.designsystem.component.TalkdeskListRowIcon
import org.talkdesk.conversation.core.designsystem.component.TalkdeskListRowMeta
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.activity.impl.R
import org.talkdesk.conversation.activity.impl.presentation.ActivityIntent
import org.talkdesk.conversation.activity.impl.presentation.ActivityItem
import org.talkdesk.conversation.activity.impl.presentation.ActivityKind
import org.talkdesk.conversation.activity.impl.presentation.CallDirection
import org.talkdesk.conversation.activity.impl.presentation.MessageChannel

/**
 * One Activity row: a call-direction or channel icon, the spelled-out sub-line ("Missed · Support
 * line"), time with unread count, and a call-back button on call rows (hidden while [offline]).
 * Screen readers get the whole row as one item with Call back as a custom action.
 */
@Composable
internal fun ActivityRow(
    item: ActivityItem,
    offline: Boolean,
    showDivider: Boolean,
    onIntent: (ActivityIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TalkdeskTheme.colors
    val kind = item.kind
    val (icon, tint) = when (kind) {
        is ActivityKind.Call -> when (kind.direction) {
            CallDirection.Inbound -> TalkdeskIcons.PhoneIn to colors.callInbound
            CallDirection.Outbound -> TalkdeskIcons.PhoneOut to colors.callOutbound
            CallDirection.Missed, CallDirection.Abandoned -> TalkdeskIcons.PhoneMissed to colors.callMissed
        }
        is ActivityKind.Message -> when (kind.channel) {
            MessageChannel.Sms -> TalkdeskIcons.Sms
            MessageChannel.WhatsApp -> TalkdeskIcons.Whatsapp
            MessageChannel.Email -> TalkdeskIcons.Mail
        } to colors.iconBrand
    }
    val kindLabel = stringResource(kind.labelRes())
    val subline = stringResource(R.string.activity_subline, kindLabel, item.detail)
    val isCall = kind is ActivityKind.Call
    val callBackLabel = stringResource(R.string.activity_call_back, item.title)
    val unread = if (item.unreadCount > 0) pluralStringResource(R.plurals.activity_unread, item.unreadCount, item.unreadCount) else null
    val description = listOfNotNull(
        stringResource(R.string.activity_row_description, kindLabel, item.title, item.detail, item.timeLabel),
        unread,
    ).joinToString(", ")
    val canCallBack = isCall && !offline

    TalkdeskListRow(
        title = item.title,
        subtitle = subline,
        showDivider = showDivider,
        onClick = { onIntent(ActivityIntent.OpenItem(item.id)) },
        modifier = modifier.clearAndSetSemantics {
            contentDescription = description
            if (canCallBack) {
                customActions = listOf(CustomAccessibilityAction(callBackLabel) { onIntent(ActivityIntent.CallBack(item.id)); true })
            }
        },
        leading = { TalkdeskListRowIcon(icon, tint) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TalkdeskListRowMeta(item.timeLabel, badgeCount = item.unreadCount.takeIf { it > 0 })
                if (isCall) {
                    if (canCallBack) {
                        CallBackButton(callBackLabel, TalkdeskIcons.Phone) { onIntent(ActivityIntent.CallBack(item.id)) }
                    } else {
                        Spacer(Modifier.size(TalkdeskTheme.sizes.touch))
                    }
                }
            }
        },
    )
}

private fun ActivityKind.labelRes(): Int = when (this) {
    is ActivityKind.Call -> when (direction) {
        CallDirection.Inbound -> R.string.activity_kind_inbound
        CallDirection.Outbound -> R.string.activity_kind_outbound
        CallDirection.Missed -> R.string.activity_kind_missed
        CallDirection.Abandoned -> R.string.activity_kind_abandoned
    }
    is ActivityKind.Message -> when (channel) {
        MessageChannel.Sms -> R.string.activity_kind_sms
        MessageChannel.WhatsApp -> R.string.activity_kind_whatsapp
        MessageChannel.Email -> R.string.activity_kind_email
    }
}

/**
 * 48dp brand icon button, as in the DS ListRow card (`ta-iconbtn--brand`).
 * TODO: replace with a `TalkdeskIconButton` once the DS has one; the design system has no icon-button component yet.
 */
@Composable
private fun CallBackButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier
            .size(TalkdeskTheme.sizes.touch)
            .clickable(role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = TalkdeskTheme.colors.iconBrand, modifier = Modifier.size(TalkdeskTheme.sizes.icon))
    }
}

@ActivityPreviews
@Composable
private fun ActivityRowPreview() = TalkdeskTheme {
    Column(Modifier.background(TalkdeskTheme.colors.bgWindow)) {
        ActivityMock.loaded().sections.first().items.forEachIndexed { index, item ->
            ActivityRow(item, offline = false, showDivider = index > 0, onIntent = {})
        }
        ActivityRow(ActivityMock.loaded().sections.first().items.first(), offline = true, showDivider = true, onIntent = {})
    }
}
