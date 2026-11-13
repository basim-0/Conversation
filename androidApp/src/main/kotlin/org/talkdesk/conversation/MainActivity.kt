package org.talkdesk.conversation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.talkdesk.conversation.auth.android.login.screens.AccountNameScreen
import org.talkdesk.conversation.auth.android.login.splashscreen.SplashTransition
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // SplashTransition's first frame is identical to the system splash, so drop it without the
        // default fade and let the in-app animation take over.
        splashScreen.setOnExitAnimationListener { it.remove() }

        setContent {
            TalkdeskTheme {
                ConversationRoot()
            }
        }
    }
}

/**
 * Temporary root until navigation and the login ViewModel exist: the splash hands off to the
 * account-name screen, which only keeps the typed name locally.
 */
@Composable
private fun ConversationRoot() {
    var splashFinished by rememberSaveable { mutableStateOf(false) }
    var accountName by rememberSaveable { mutableStateOf("") }

    // TODO: pass the session check's readiness once it exists.
    SplashTransition(isReady = true, onFinished = { splashFinished = true }) {
        AccountNameScreen(
            accountName = accountName,
            onAccountNameChange = { accountName = it },
            onContinue = {},
            onHelpClick = {},
            showHelpLink = false,
            requestFocus = splashFinished,
        )
    }
}
