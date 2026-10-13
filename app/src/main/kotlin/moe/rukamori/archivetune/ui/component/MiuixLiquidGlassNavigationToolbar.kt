/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

import android.os.SystemClock
import android.view.ViewConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import moe.rukamori.archivetune.ui.screens.Screens
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Miuix-styled replacement for [FloatingNavigationToolbar]. Same surface
 * contract (items, selection, double-tap on Search) so MainActivity does
 * not need to change.
 *
 * Note: the liquid-glass implementation from the Miuix sample tree has been
 * removed because its API does not match the shipped miuix-blur 0.9.4
 * artifact. The plain [FloatingNavigationBar] is used instead, which ships
 * in the stable library and compiles without extra samples.
 */
@Composable
fun MiuixLiquidGlassNavigationToolbar(
    items: List<Screens>,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
    isSelected: (Screens) -> Boolean,
    onItemClick: (Screens, Boolean) -> Unit,
    onSearchItemDoubleClick: (() -> Unit)? = null,
) {
    val navigationContainerColor =
        if (pureBlack) Color.Black else MiuixTheme.colorScheme.surfaceContainer

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)),
        contentAlignment = Alignment.Center,
    ) {
        FloatingNavigationBar(
            color = navigationContainerColor,
            defaultWindowInsetsPadding = false,
        ) {
            items.forEach { screen ->
                val selected = isSelected(screen)
                val lastClickTime = remember(screen) { mutableLongStateOf(0L) }
                val onClick =
                    remember(screen, selected, onItemClick, onSearchItemDoubleClick) {
                        {
                            val currentTime = SystemClock.uptimeMillis()
                            val doubleTapHandler =
                                if (screen === Screens.Search) onSearchItemDoubleClick else null
                            val isDoubleClick =
                                doubleTapHandler != null &&
                                    currentTime - lastClickTime.longValue <=
                                    ViewConfiguration.getDoubleTapTimeout()
                            lastClickTime.longValue = if (isDoubleClick) 0L else currentTime
                            if (isDoubleClick) {
                                doubleTapHandler?.invoke()
                                Unit
                            } else {
                                onItemClick(screen, selected)
                            }
                        }
                    }
                FloatingNavigationBarItem(
                    selected = selected,
                    onClick = onClick,
                    icon = screen.miuixIcon(selected),
                    label = stringResource(screen.titleId),
                )
            }
        }
    }
}

private fun Screens.miuixIcon(selected: Boolean): ImageVector =
    when (this) {
        is Screens.Home -> if (selected) Icons.Rounded.Home else Icons.Outlined.Home
        is Screens.Search -> Icons.Rounded.Search
        is Screens.Library ->
            if (selected) Icons.Rounded.VideoLibrary else Icons.Outlined.VideoLibrary
        else -> Icons.Rounded.Category
    }
