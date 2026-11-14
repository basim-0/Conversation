package org.talkdesk.conversation.calling.impl.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Returns a function that runs an action only once the microphone permission is granted, asking for it
 * first when needed. [onDenied] runs when the agent refuses.
 */
@Composable
internal fun rememberMicrophoneGate(onDenied: () -> Unit): (action: () -> Unit) -> Unit {
    val context = LocalContext.current
    val currentOnDenied by rememberUpdatedState(onDenied)
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val action = pending
        pending = null
        if (granted) action?.invoke() else currentOnDenied()
    }
    return remember(context, launcher) {
        { action ->
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            if (granted) {
                action()
            } else {
                pending = action
                launcher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
}
