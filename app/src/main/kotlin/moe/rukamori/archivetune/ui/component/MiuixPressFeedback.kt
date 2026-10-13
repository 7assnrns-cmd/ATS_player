/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import moe.rukamori.archivetune.constants.MiuixThemeEnabledKey
import moe.rukamori.archivetune.utils.rememberPreference
import top.yukonga.miuix.kmp.utils.SinkFeedback

/**
 * Drop-in replacement for [Modifier.clickable] that uses Miuix's
 * SinkFeedback indication when the Miuix theme is enabled, and falls back
 * to the standard Material ripple otherwise. This keeps the app visually
 * consistent whichever theme is active.
 */
@Composable
fun Modifier.miuixClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier {
    val miuixEnabled by rememberPreference(MiuixThemeEnabledKey, defaultValue = false)
    if (!miuixEnabled) {
        return this.clickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onClick = onClick,
        )
    }
    val source = remember { MutableInteractionSource() }
    val indication = remember { SinkFeedback() }
    return this.clickable(
        interactionSource = source,
        indication = indication,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = onClick,
    )
}

/**
 * Drop-in replacement for [Modifier.combinedClickable] with the same
 * Miuix-or-Material behaviour as [miuixClickable].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.miuixCombinedClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit,
): Modifier {
    val miuixEnabled by rememberPreference(MiuixThemeEnabledKey, defaultValue = false)
    if (!miuixEnabled) {
        return this.combinedClickable(
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onLongClickLabel = onLongClickLabel,
            onLongClick = onLongClick,
            onDoubleClick = onDoubleClick,
            onClick = onClick,
        )
    }
    val source = remember { MutableInteractionSource() }
    val indication = remember { SinkFeedback() }
    return this.combinedClickable(
        interactionSource = source,
        indication = indication,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        onClick = onClick,
    )
}
