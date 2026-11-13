package org.talkdesk.conversation.home.impl.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/** Phone-sized preview in both themes, matching the 360×800 Android board frames. */
@Preview(name = "Light", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class HomePreviews
