package org.talkdesk.conversation.account.impl.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.talkdesk.conversation.account.impl.presentation.AccountViewModel

/** Bindings of the Account feature. The host provides nothing. */
val accountModule = module {
    viewModel { AccountViewModel() }
}
