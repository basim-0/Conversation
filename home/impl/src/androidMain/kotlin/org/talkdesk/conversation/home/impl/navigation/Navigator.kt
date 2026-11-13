package org.talkdesk.conversation.home.impl.navigation

import androidx.navigation3.runtime.NavKey

/** Applies navigation events to [NavigationState]. */
internal class Navigator(private val state: NavigationState) {

    /** Switches to a tab, or pushes a sub-route onto the selected tab's back stack. */
    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    /** Pops the selected tab's stack; at its base, returns to the start tab. */
    fun goBack() {
        val stack = state.backStacks[state.topLevelRoute] ?: error("No back stack for ${state.topLevelRoute}")
        if (stack.last() == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            stack.removeLastOrNull()
        }
    }
}
