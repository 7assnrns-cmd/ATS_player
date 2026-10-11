package moe.rukamori.archivetune.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.currentValueOf
import kotlinx.coroutines.launch

/**
 * Wraps the current indication (the Material ripple) so that every pressable
 * element also compresses slightly while pressed and springs back with a small
 * overshoot on release. Does nothing when [enabled] is false or when the
 * current indication is not a node-based one. On the glass top bar it also draws
 * a soft contrast halo behind small pressable icons.
 */
@Composable
fun ProvideBouncyIndication(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    val base = LocalIndication.current
    if (!enabled || base !is IndicationNodeFactory) {
        content()
        return
    }
    val bouncy = remember(base) { BouncyIndication(base) }
    CompositionLocalProvider(LocalIndication provides bouncy, content = content)
}

private class BouncyIndication(
    private val base: IndicationNodeFactory,
) : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        BouncyIndicationNode(interactionSource, base.create(interactionSource))

    override fun equals(other: Any?): Boolean = other is BouncyIndication && other.base == base

    override fun hashCode(): Int = base.hashCode() * 31 + 1
}

private class BouncyIndicationNode(
    private val interactionSource: InteractionSource,
    baseNode: DelegatableNode,
) : DelegatingNode(),
    DrawModifierNode,
    CompositionLocalConsumerModifierNode {
    // 0 = at rest, 1 = fully pressed; goes slightly below 0 on release for the bounce.
    private val pressProgress = Animatable(0f)

    init {
        delegate(baseNode)
    }

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> launch { press() }
                    is PressInteraction.Release -> launch { release() }
                    is PressInteraction.Cancel -> launch { release() }
                }
            }
        }
    }

    private suspend fun press() {
        pressProgress.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
    }

    private suspend fun release() {
        pressProgress.animateTo(
            0f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        )
    }

    override fun ContentDrawScope.draw() {
        drawContrastHalo()
        val progress = pressProgress.value
        if (progress == 0f) {
            drawContent()
            return
        }
        // Small elements shrink more than wide rows so large items do not jump around.
        val minDp = size.minDimension / density
        val shrink =
            when {
                minDp < 64f -> 0.10f
                minDp < 160f -> 0.05f
                else -> 0.02f
            }
        val factor = 1f - shrink * progress
        withTransform({ scale(factor, factor, center) }) {
            this@draw.drawContent()
        }
    }

    // Soft radial halo behind small icons on the glass top bar. Its colour opposes the
    // icon colour so the icon stays readable over any blurred content.
    private fun ContentDrawScope.drawContrastHalo() {
        if (currentValueOf(LocalGlassBackdrop) == null) return
        if (!currentValueOf(LocalGlassBarHalo)) return
        if (size.minDimension / density >= 64f) return
        val iconIsLight = currentValueOf(LocalContentColor).luminance() > 0.5f
        val halo = if (iconIsLight) Color.Black else Color.White
        val radius = size.minDimension * 0.78f
        drawCircle(
            brush =
                Brush.radialGradient(
                    colors = listOf(halo.copy(alpha = 0.18f), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
            radius = radius,
            center = center,
        )
    }
}
