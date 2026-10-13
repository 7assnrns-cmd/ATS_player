/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import component.liquid.IosLiquidGlassNavigationBar
import moe.rukamori.archivetune.ui.screens.Screens
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.blur.LayerBackdrop

/**
 * ArchiveTune wrapper around the Miuix Liquid Glass navigation bar from the
 * upstream example tree. Translates the app's [Screens] list into Miuix
 * [NavigationItem]s and forwards the surrounding [LayerBackdrop] so the bar
 * can blur and refract the content behind it.
 *
 * When [appBackdrop] is null (e.g. during the first launch of a release that
 * has not yet wired the backdrop in MainActivity), the bar renders without
 * the live refraction but keeps its shape, colours and press behaviour.
 */
@Composable
fun MiuixLiquidGlassNavigationToolbar(
    items: List<Screens>,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
    isSelected: (Screens) -> Boolean,
    onItemClick: (Screens, Boolean) -> Unit,
    onSearchItemDoubleClick: (() -> Unit)? = null,
    appBackdrop: LayerBackdrop? = null,
) {
    val selectedIndex =
        remember(items, isSelected) {
            items.indexOfFirst { isSelected(it) }.coerceAtLeast(0)
        }

    // Resolve labels through stringResource here because NavigationItem
    // stores plain strings, not resource IDs.
    val navigationItems =
        items.map { screen ->
            NavigationItem(
                label = stringResource(screen.titleId),
                icon = screen.miuixIcon(isSelected(screen)),
            )
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)),
        contentAlignment = Alignment.Center,
    ) {
        IosLiquidGlassNavigationBar(
            items = navigationItems,
            selectedIndex = selectedIndex,
            onItemClick = { index ->
                val screen = items.getOrNull(index) ?: return@IosLiquidGlassNavigationBar
                onItemClick(screen, isSelected(screen))
            },
            backdrop = appBackdrop,
            isBlurActive = appBackdrop != null,
        )
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
