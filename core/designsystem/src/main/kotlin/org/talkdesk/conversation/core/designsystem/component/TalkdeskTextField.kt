package org.talkdesk.conversation.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalContentColor
import org.talkdesk.conversation.core.designsystem.icon.TalkdeskIcons
import org.talkdesk.conversation.core.designsystem.theme.TalkdeskTheme

/**
 * Labelled single-line input (`ta-field`). The label sits above a 48dp box (padding 12, radius 4,
 * 1dp `border-control`). Focused: 2dp `border-focus`. Error: 2dp `border-error` and the helper in
 * `text-error` with an alert icon — the error text should say what to do, not just what failed.
 * Disabled: `surface-sunken` fill, `text-disabled`.
 *
 * @param helperText shown under the field; replaced by [errorText] when that is non-null.
 * @param prefix e.g. the country selector of a phone input; drawn with a divider after it.
 */
@Composable
fun TalkdeskTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    required: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    prefix: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource? = null,
) {
    val colors = TalkdeskTheme.colors
    val typography = TalkdeskTheme.typography
    val shape = TalkdeskTheme.shapes.sm
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val isError = errorText != null

    val (borderWidth, borderColor) = when {
        !enabled -> 1.dp to colors.borderDivider
        isError -> 2.dp to colors.borderError
        focused -> 2.dp to colors.borderFocus
        else -> 1.dp to colors.borderControl
    }
    val textColor = if (enabled) colors.textDefault else colors.textDisabled

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = buildAnnotatedString {
                append(label)
                if (required) withStyle(SpanStyle(color = colors.textError)) { append(" *") }
            },
            style = typography.label,
            color = if (enabled) colors.textDefault else colors.textDisabled,
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (isError) Modifier.semantics { error(errorText.orEmpty()) } else Modifier),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = true,
            textStyle = typography.body.copy(color = textColor),
            cursorBrush = SolidColor(colors.borderFocus),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            interactionSource = source,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = TalkdeskTheme.sizes.input)
                        .background(if (enabled) colors.surface1 else colors.surfaceSunken, shape)
                        .border(borderWidth, borderColor, shape)
                        // Keep text from shifting when the border thickens (CSS: padding 12 → 11).
                        .padding(horizontal = 12.dp - (borderWidth - 1.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CompositionLocalProvider(LocalContentColor provides colors.iconMuted) {
                        if (prefix != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 4.dp),
                            ) {
                                CompositionLocalProvider(LocalContentColor provides colors.textDefault) { prefix() }
                                Box(
                                    Modifier
                                        .padding(start = 8.dp)
                                        .size(width = 1.dp, height = 24.dp)
                                        .background(colors.borderDivider),
                                )
                            }
                        }
                        leading?.invoke()
                        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty() && placeholder != null) {
                                Text(placeholder, style = typography.body, color = colors.textPlaceholder, maxLines = 1)
                            }
                            innerTextField()
                        }
                        trailing?.invoke()
                    }
                }
            },
        )

        val help = errorText ?: helperText
        if (help != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isError) {
                    Icon(TalkdeskIcons.Alert, contentDescription = null, tint = colors.textError, modifier = Modifier.size(16.dp))
                }
                Text(help, style = typography.caption, color = if (isError) colors.textError else colors.textDescription)
            }
        }
    }
}

@TalkdeskPreviews
@Composable
private fun TalkdeskTextFieldPreview() = PreviewSurface {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        var email by remember { mutableStateOf("") }
        TalkdeskTextField(email, { email = it }, "Email", placeholder = "name@company.com", helperText = "Use your Talkdesk account email", required = true)
        TalkdeskTextField(
            "secret", {}, "Password",
            visualTransformation = PasswordVisualTransformation(),
            trailing = { Icon(TalkdeskIcons.Lock, null, Modifier.size(20.dp)) },
        )
        TalkdeskTextField("acme-support", {}, "Account", errorText = "We couldn't find this account")
        TalkdeskTextField("204", {}, "Extension", enabled = false)
        TalkdeskTextField(
            "555 0142", {}, "Phone number",
            prefix = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("US +1", style = TalkdeskTheme.typography.body)
                    Icon(TalkdeskIcons.ChevDown, null, Modifier.size(20.dp))
                }
            },
        )
    }
}
