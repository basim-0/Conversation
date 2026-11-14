package org.talkdesk.conversation.calling.impl.ui.dialer.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.domain.model.DialCountry
import org.talkdesk.conversation.calling.impl.ui.CallingPreviews
import org.talkdesk.conversation.calling.impl.ui.PreviewSurface
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Dial-code selector of the dialer: flag, "+1" and a chevron in a 48dp outlined button
 * (`border-control`, `radius-sm`). Private to the calling feature: the design system has no such control.
 */
@Composable
internal fun CountryButton(country: DialCountry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = TalkdeskTheme.colors
    val description = stringResource(R.string.calling_country_picker, country.name, country.dialCode)
    Row(
        modifier
            .height(48.dp)
            .clip(TalkdeskTheme.shapes.sm)
            .border(1.dp, colors.borderControl, TalkdeskTheme.shapes.sm)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 12.dp, end = 8.dp)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CountryFlag(country.isoCode)
        Text(country.dialCode, style = TalkdeskTheme.typography.bodyStrong, color = colors.textDefault)
        Icon(TalkdeskIcons.ChevDown, contentDescription = null, tint = colors.iconDefault, modifier = Modifier.size(TalkdeskTheme.sizes.iconSmall))
    }
}

@CallingPreviews
@Composable
private fun CountryButtonPreview() = PreviewSurface {
    CountryButton(DialCountry("US", "United States", "+1"), onClick = {})
}
