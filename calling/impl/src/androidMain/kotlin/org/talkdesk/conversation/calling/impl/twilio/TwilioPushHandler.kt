package org.talkdesk.conversation.calling.impl.twilio

import org.talkdesk.conversation.core.push.PushHandler
import org.talkdesk.conversation.core.push.PushMessage

/** Passes Twilio's call-invite pushes (marked by `twi_message_type`) to the voice SDK; ignores other pushes. */
internal class TwilioPushHandler(private val gateway: TwilioVoiceGateway) : PushHandler {
    override fun handle(message: PushMessage) {
        if (message.data.containsKey(TWILIO_MESSAGE_TYPE)) gateway.handlePush(message.data)
    }

    private companion object {
        const val TWILIO_MESSAGE_TYPE = "twi_message_type"
    }
}
