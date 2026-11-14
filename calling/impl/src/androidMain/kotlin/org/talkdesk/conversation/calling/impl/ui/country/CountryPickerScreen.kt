package org.talkdesk.conversation.calling.impl.ui.country

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.calling.impl.domain.model.DialCountry
import org.talkdesk.conversation.calling.impl.presentation.country.CountryPickerIntent
import org.talkdesk.conversation.calling.impl.presentation.country.CountryPickerState
import org.talkdesk.conversation.calling.impl.ui.CallingPreviews
import org.talkdesk.conversation.calling.impl.ui.dialer.components.CountryFlag
import org.talkdesk.conversation.core.designsystem.component.TalkdeskEmptyState
import org.talkdesk.conversation.core.designsystem.component.TalkdeskIconButton
import org.talkdesk.conversation.core.designsystem.component.TalkdeskListRow
import org.talkdesk.conversation.core.designsystem.component.TalkdeskSearchField
import org.talkdesk.conversation.core.designsystem.component.TalkdeskToolbar
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Country list opened from the dialer: a toolbar, a search field (name or dial code) and every country with
 * its flag and dial code, the picked one marked with a check. Built only from design-system components.
 * Stateless: renders [state] and reports taps as [CountryPickerIntent]s.
 */
@Composable
fun CountryPickerScreen(
    state: CountryPickerState,
    onIntent: (CountryPickerIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TalkdeskTheme.colors
    val spacing = TalkdeskTheme.spacing
    Column(
        modifier
            .fillMaxSize()
            .background(colors.bgWindow)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        TalkdeskToolbar(
            title = stringResource(R.string.calling_country_title),
            navigationIcon = {
                TalkdeskIconButton(TalkdeskIcons.Back, stringResource(R.string.calling_back), onClick = { onIntent(CountryPickerIntent.Back) })
            },
        )
        TalkdeskSearchField(
            query = state.query,
            onQueryChange = { onIntent(CountryPickerIntent.QueryChanged(it)) },
            placeholder = stringResource(R.string.calling_country_search),
            clearContentDescription = stringResource(R.string.calling_country_search_clear),
            modifier = Modifier.fillMaxWidth().padding(horizontal = spacing.space16, vertical = spacing.space8),
        )
        if (state.isEmptyResult) {
            TalkdeskEmptyState(
                icon = TalkdeskIcons.Search,
                title = stringResource(R.string.calling_country_empty_title),
                body = stringResource(R.string.calling_country_empty_body),
            )
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(state.countries, key = { it.isoCode }) { country ->
                    CountryRow(
                        country = country,
                        selected = country.isoCode == state.selectedIsoCode,
                        onClick = { onIntent(CountryPickerIntent.Select(country.isoCode)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CountryRow(country: DialCountry, selected: Boolean, onClick: () -> Unit) {
    val colors = TalkdeskTheme.colors
    TalkdeskListRow(
        title = country.name,
        leading = { CountryFlag(country.isoCode) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(TalkdeskTheme.spacing.space8)) {
                Text(country.dialCode, style = TalkdeskTheme.typography.body, color = colors.textDescription)
                if (selected) {
                    Icon(TalkdeskIcons.Check, contentDescription = null, tint = colors.iconBrand, modifier = Modifier.size(TalkdeskTheme.sizes.icon))
                }
            }
        },
        onClick = onClick,
    )
}

private val PreviewCountries = listOf(
    DialCountry("DE", "Germany", "+49"),
    DialCountry("PT", "Portugal", "+351"),
    DialCountry("US", "United States", "+1"),
)

@CallingPreviews
@Composable
private fun CountryPickerPreview() = TalkdeskTheme {
    CountryPickerScreen(CountryPickerState(countries = PreviewCountries, selectedIsoCode = "PT", isLoading = false), onIntent = {})
}

@CallingPreviews
@Composable
private fun CountryPickerEmptyPreview() = TalkdeskTheme {
    CountryPickerScreen(CountryPickerState(query = "zzz", isLoading = false), onIntent = {})
}
