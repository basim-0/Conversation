package org.talkdesk.conversation.calling.impl.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.talkdesk.conversation.calling.impl.presentation.call.CallViewModel
import org.talkdesk.conversation.calling.impl.presentation.dialer.DialerViewModel
import org.talkdesk.conversation.calling.impl.presentation.incoming.IncomingCallViewModel

/** Bindings of the Calling feature. The host provides nothing. */
val callingModule = module {
    viewModel { DialerViewModel() }
    viewModel { CallViewModel() }
    viewModel { IncomingCallViewModel() }
}
