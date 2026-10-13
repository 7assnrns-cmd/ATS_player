/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.constants.MiuixThemeEnabledKey
import moe.rukamori.archivetune.utils.rememberPreference

/**
 * Entry animation for lazy-list items. Each item slides up a small amount,
 * fades in, and scales from 96% to its natural size. Items are staggered by
 * index up to a small cap so long lists do not feel sluggish.
 *
 * The animation plays only once per item instance and only when the Miuix
 * theme is enabled. When it is disabled, the item is composed at its final
 * state and no allocation is made.
 */
@Composable
fun StaggeredEntryItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val miuixEnabled by rememberPreference(MiuixThemeEnabledKey, defaultValue = false)
    val density = LocalDensity.current
    val startOffsetPx = with(density) { EntryOffset.toPx() }

    val offsetYAnim = remember { Animatable(if (miuixEnabled) startOffsetPx else 0f) }
    val alphaAnim = remember { Animatable(if (miuixEnabled) 0f else 1f) }
    val scaleAnim = remember { Animatable(if (miuixEnabled) EntryStartScale else 1f) }
    var hasAnimated by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(miuixEnabled) {
        if (!miuixEnabled) {
            offsetYAnim.snapTo(0f)
            alphaAnim.snapTo(1f)
            scaleAnim.snapTo(1f)
            return@LaunchedEffect
        }
        if (hasAnimated) return@LaunchedEffect
        hasAnimated = true
        offsetYAnim.snapTo(startOffsetPx)
        alphaAnim.snapTo(0f)
        scaleAnim.snapTo(EntryStartScale)
        val effectiveIndex = index.coerceIn(0, MaxStaggerIndex)
        if (effectiveIndex > 0) delay(effectiveIndex * StaggerMs)
        launch { offsetYAnim.animateTo(0f, tween(DurationMs, easing = GlassEasing)) }
        launch { alphaAnim.animateTo(1f, tween(DurationMs, easing = GlassEasing)) }
        launch { scaleAnim.animateTo(1f, tween(DurationMs, easing = GlassEasing)) }
    }

    Box(
        modifier.graphicsLayer {
            translationY = offsetYAnim.value
            this.alpha = alphaAnim.value
            scaleX = scaleAnim.value
            scaleY = scaleAnim.value
        }
    ) { content() }
}

/**
 * Drop-in replacement for [LazyListScope.items] that wraps every item in
 * [StaggeredEntryItem] and provides the per-item index required by the
 * stagger calculation. Lambda signature matches the original, so call sites
 * change by one word.
 */
inline fun <T> LazyListScope.staggeredItems(
    items: List<T>,
    noinline key: ((item: T) -> Any)? = null,
    noinline contentType: (item: T) -> Any? = { null },
    crossinline itemContent: @Composable (item: T) -> Unit,
) {
    itemsIndexed(
        items = items,
        key = key?.let { { _, item -> it(item) } },
        contentType = { _, item -> contentType(item) },
    ) { index, item ->
        StaggeredEntryItem(index = index) {
            itemContent(item)
        }
    }
}

private val EntryOffset = 20.dp
private const val EntryStartScale = 0.96f
private const val DurationMs = 420
private const val StaggerMs = 50L
private const val MaxStaggerIndex = 8
