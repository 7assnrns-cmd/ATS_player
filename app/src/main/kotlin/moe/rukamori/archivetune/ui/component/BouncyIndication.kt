package moe.rukamori.archivetune.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.launch

/**
 * Wraps the current indication (the Material ripple) so that every pressable
 * element also compresses slightly while pressed and springs back with a small
 * overshoot on release. Does nothing when [enabled] is false or when the
 * current indication is not a node-based one.
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
    DrawModifierNode {
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
}
