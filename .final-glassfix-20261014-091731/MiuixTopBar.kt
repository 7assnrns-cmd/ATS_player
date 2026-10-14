/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixNativeTopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Miuix-styled top bar backed by the native Miuix TopAppBar from
 * compose-miuix-ui. It preserves the original Composable-title contract so
 * existing callers (MainActivity) need no changes, while inheriting Miuix's
 * own sizing, insets, colors, and spring animations.
 *
 * The native Miuix TopAppBar takes a String title. To keep our Composable
 * title slot (used for an icon + styled text), we render it inside the
 * navigationIcon slot, which is a @Composable lambda.
 */
@Composable
fun MiuixTopBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MiuixTheme.colorScheme.surface,
    contentColor: Color = MiuixTheme.colorScheme.onSurface,
) {
    MiuixNativeTopAppBar(
        title = "",
        modifier = modifier.fillMaxWidth(),
        color = containerColor,
        navigationIcon = {
            Row(
                modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                navigationIcon()
                if (title !is @Composable () -> Unit) {
                    // unreachable, keeps type checker happy
                }
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    CompositionLocalProvider(
                        androidx.compose.material3.LocalContentColor provides contentColor,
                    ) {
                        title()
                    }
                }
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                actions()
            }
        },
    )
}

/**
 * Optional alternative that mirrors the previous 56dp-tall custom bar.
 * Kept for callers that want a fixed height independent of Miuix metrics.
 */
@Composable
fun MiuixTopBarFixed(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MiuixTheme.colorScheme.surface,
    contentColor: Color = MiuixTheme.colorScheme.onSurface,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon()
            Box(
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides contentColor,
                ) {
                    title()
                }
            }
            Spacer(Modifier.width(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                actions()
            }
        }
    }
}
