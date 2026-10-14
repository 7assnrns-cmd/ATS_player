/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import moe.rukamori.archivetune.constants.MiuixForDialogsKey
import moe.rukamori.archivetune.constants.MiuixForNavigationBarKey
import moe.rukamori.archivetune.constants.MiuixForSettingsKey
import moe.rukamori.archivetune.constants.MiuixForSheetsKey
import moe.rukamori.archivetune.constants.MiuixForTopBarKey
import moe.rukamori.archivetune.constants.MiuixThemeEnabledKey
import moe.rukamori.archivetune.utils.rememberPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

/** The areas that can be independently switched to Miuix. */
enum class MiuixArea {
    SETTINGS,
    TOP_BAR,
    NAVIGATION_BAR,
    DIALOGS,
    SHEETS,
}

/** True when the master toggle is on. */
val LocalMiuixEnabled = staticCompositionLocalOf { false }

/**
 * Wraps [content] in MiuixTheme when the master toggle AND the given
 * [area] toggle are both enabled. When disabled, the content is
 * rendered unchanged — no Miuix dependency is touched.
 */
@Composable
fun MiuixScope(
    area: MiuixArea,
    content: @Composable () -> Unit,
) {
    val master by rememberPreference(MiuixThemeEnabledKey, defaultValue = true)
    if (!master) {
        content()
        return
    }

    val areaEnabled =
        when (area) {
            MiuixArea.SETTINGS ->
                rememberPreference(MiuixForSettingsKey, defaultValue = true).value
            MiuixArea.TOP_BAR ->
                rememberPreference(MiuixForTopBarKey, defaultValue = true).value
            MiuixArea.NAVIGATION_BAR ->
                rememberPreference(MiuixForNavigationBarKey, defaultValue = true).value
            MiuixArea.DIALOGS ->
                rememberPreference(MiuixForDialogsKey, defaultValue = true).value
            MiuixArea.SHEETS ->
                rememberPreference(MiuixForSheetsKey, defaultValue = true).value
        }

    if (!areaEnabled) {
        content()
        return
    }

    val dark = isSystemInDarkTheme()
    val colors = remember(dark) { if (dark) darkColorScheme() else lightColorScheme() }

    CompositionLocalProvider(LocalMiuixEnabled provides true) {
        MiuixTheme(colors = colors) { content() }
    }
}
