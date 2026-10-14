/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * HyperOS-style reveal animation.
 *
 * Wraps [content] and animates it on first composition:
 *   - fadeIn over [durationMillis]
 *   - scaleIn from [initialScale] to 1.0
 *   - optional [index]-based stagger so lists cascade in sequence
 *
 * Use it inside `LazyColumn` / `LazyGrid` / `LazyRow` to bring cards and
 * tiles on-screen the way HyperOS surfaces do — each one grows slightly
 * and fades up rather than snapping into place.
 *
 *     LazyColumn {
 *         itemsIndexed(items) { index, item ->
 *             HyperOsReveal(index = index) {
 *                 Card { ... }
 *             }
 *         }
 *     }
 *
 * For long lists, cap the index (e.g. `index % 8`) so items arriving on
 * scroll don't wait an unbounded amount of time.
 */
@Composable
fun HyperOsReveal(
    modifier: Modifier = Modifier,
    index: Int = 0,
    initialScale: Float = 0.92f,
    durationMillis: Int = 320,
    staggerStepMillis: Int = 30,
    maxStaggerMillis: Int = 240,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val delayMs = (index * staggerStepMillis).coerceAtMost(maxStaggerMillis)
        if (delayMs > 0) delay(delayMs.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter =
            fadeIn(animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)) +
                scaleIn(
                    initialScale = initialScale,
                    animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
                ),
        exit = fadeOut(animationSpec = tween(0)),
    ) {
        content()
    }
}

/**
 * Modifier variant: same reveal, without staggering. Applies scale and alpha
 * directly to the target via graphicsLayer. Use when wrapping in a composable
 * would break the layout (e.g. inside `Modifier.align` or `Modifier.weight`).
 */
@Composable
fun rememberHyperOsRevealModifier(
    initialScale: Float = 0.92f,
    durationMillis: Int = 320,
): Modifier {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else initialScale,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "hyperOsRevealScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing),
        label = "hyperOsRevealAlpha",
    )

    return Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

/**
 * A convenience wrapper that reveals each item in a list with stagger.
 * Kept small so files can use it as a drop-in around a `Card { ... }`.
 */
@Composable
fun HyperOsRevealBox(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    HyperOsReveal(
        modifier = modifier,
        index = index,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
