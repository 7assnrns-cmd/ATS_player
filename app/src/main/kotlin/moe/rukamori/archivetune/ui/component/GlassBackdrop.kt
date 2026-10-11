package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy

/**
 * Backdrop captured from the screen content behind the glass.
 * Null means glass is disabled and surfaces keep their normal look.
 */
val LocalGlassBackdrop = compositionLocalOf<Backdrop?> { null }

/**
 * Modifier that marks a layout as the source of the glass backdrop.
 * Provided by the theme; an empty Modifier means glass is disabled.
 */
val LocalGlassSource = compositionLocalOf<Modifier> { Modifier }

/**
 * Low-level frosted panel: blurs and saturates whatever [backdrop] shows
 * behind this element, then paints [fill] on top. Returns the receiver
 * unchanged when [backdrop] is null.
 */
fun Modifier.glassPanel(
    backdrop: Backdrop?,
    shape: Shape = RoundedCornerShape(24.dp),
    fill: Brush =
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.16f)),
        ),
    blurRadius: Dp = 20.dp,
): Modifier =
    if (backdrop == null) {
        this
    } else {
        this.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(blurRadius.toPx())
            },
            onDrawSurface = { drawRect(fill) },
        )
    }

/**
 * App-styled glass: the panel colour is the theme surface mixed with the
 * current primary (so it follows the playing track's cover), denser at the
 * top and lighter at the bottom, with a 1dp edge that catches light from the
 * top-left. No-op while [LocalGlassBackdrop] is null.
 */
@Composable
fun Modifier.glassSurface(
    shape: Shape,
    accentMix: Float = 0.12f,
    blurRadius: Dp = 20.dp,
): Modifier {
    val backdrop = LocalGlassBackdrop.current ?: return this
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    val body = lerp(scheme.surface, scheme.primary, accentMix)
    val fill =
        Brush.verticalGradient(
            listOf(
                body.copy(alpha = if (dark) 0.46f else 0.58f),
                body.copy(alpha = if (dark) 0.30f else 0.40f),
            ),
        )
    val edge =
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = if (dark) 0.34f else 0.70f),
                Color.White.copy(alpha = 0.06f),
                Color.White.copy(alpha = if (dark) 0.14f else 0.30f),
            ),
        )
    return this
        .glassPanel(backdrop = backdrop, shape = shape, fill = fill, blurRadius = blurRadius)
        .border(width = 1.dp, brush = edge, shape = shape)
}
