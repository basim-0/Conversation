package org.talkdesk.conversation.calling.impl.ui.dialer.components

import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.talkdesk.conversation.calling.impl.R
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * 24x16 flag of a country, from the flag images the legacy app used (one per ISO code). A country without an
 * image shows a globe. Decorative: the country is always named next to it.
 */
@Composable
internal fun CountryFlag(isoCode: String, modifier: Modifier = Modifier) {
    val resources = LocalContext.current.resources
    val flag = remember(resources, isoCode) { flagResources(resources)[isoCode] }
    if (flag != null) {
        Image(
            painter = painterResource(flag),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(24.dp, 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .border(1.dp, TalkdeskTheme.colors.borderDivider, RoundedCornerShape(2.dp)),
        )
    } else {
        Icon(
            TalkdeskIcons.Globe,
            contentDescription = null,
            tint = TalkdeskTheme.colors.iconDefault,
            modifier = modifier.size(24.dp, 16.dp),
        )
    }
}

private var flagCache: Map<String, Int>? = null

/**
 * ISO code -> drawable, read from the `country_list_flags` array (named `country_list_<iso>_flag`). Going
 * through the array keeps the resource shrinker from removing the images.
 */
private fun flagResources(resources: Resources): Map<String, Int> = flagCache ?: buildMap {
    val array = resources.obtainTypedArray(R.array.country_list_flags)
    try {
        for (index in 0 until array.length()) {
            @DrawableRes val id = array.getResourceId(index, 0)
            if (id == 0) continue
            val iso = resources.getResourceEntryName(id).split("_").getOrNull(FLAG_ISO_TOKEN)?.uppercase()
            if (iso != null && iso.length == ISO_LENGTH) put(iso, id)
        }
    } finally {
        array.recycle()
    }
}.also { flagCache = it }

private const val FLAG_ISO_TOKEN = 2
private const val ISO_LENGTH = 2
