package org.talkdesk.conversation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme
import org.talkdesk.conversation.navigation.ConversationRoot

class MainActivity : ComponentActivity() {
    /** Sign-in redirects delivered by deep link (Custom Tab fallback). */
    private val redirects = Channel<String>(Channel.BUFFERED)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // SplashTransition's first frame is identical to the system splash, so drop it without the
        // default fade and let the in-app animation take over.
        splashScreen.setOnExitAnimationListener { it.remove() }

        // Only a fresh start: after recreation the same intent would deliver the redirect twice.
        if (savedInstanceState == null) forwardRedirect(intent)

        setContent {
            TalkdeskTheme {
                ConversationRoot(redirects = redirects.receiveAsFlow())
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        forwardRedirect(intent)
    }

    private fun forwardRedirect(intent: Intent) {
        val data = intent.data ?: return
        if (data.scheme == REDIRECT_SCHEME && data.host == REDIRECT_HOST) redirects.trySend(data.toString())
    }

    private companion object {
        const val REDIRECT_SCHEME = "com.talkdesk.agent"
        const val REDIRECT_HOST = "auth"
    }
}
