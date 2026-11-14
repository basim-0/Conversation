package org.talkdesk.conversation.calling.impl.ui.call.components

/** Call length as `mm:ss`, or `h:mm:ss` from one hour. Digits only, so it needs no translation. */
internal fun formatElapsed(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    fun two(n: Int) = n.toString().padStart(2, '0')
    return if (hours > 0) "$hours:${two(minutes)}:${two(seconds)}" else "${two(minutes)}:${two(seconds)}"
}
