package org.talkdesk.conversation.calling.impl.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Hosts the ringing and in-call screens. It opens over the lock screen from the incoming-call notification's
 * full-screen intent, from the ongoing-call notification and from the dialer, and closes when the call is over.
 */
internal class CallActivity : ComponentActivity() {

    /** Counts "answer" requests from the notification button, so each one is handled once. */
    private var answerRequests by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        if (intent.action == ACTION_ANSWER) answerRequests++
        setContent {
            TalkdeskTheme {
                CallHost(answerRequests = answerRequests, onClose = ::finish)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == ACTION_ANSWER) answerRequests++
    }

    @Suppress("DEPRECATION")
    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
    }

    companion object {
        private const val ACTION_OPEN = "org.talkdesk.conversation.calling.action.OPEN_CALL"
        private const val ACTION_ANSWER = "org.talkdesk.conversation.calling.action.ANSWER_CALL"

        /** Intent that shows the call screens; with [answer] it also answers the ringing call. */
        fun intent(context: Context, answer: Boolean = false): Intent =
            Intent(context, CallActivity::class.java)
                .setAction(if (answer) ACTION_ANSWER else ACTION_OPEN)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
