package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
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
import com.kyant.backdrop.effects.lens
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
 * behind this element, then paints [fill] on top. Optional [lensHeight] and
 * [lensAmount] add edge refraction; a specified [hueTint] shifts the hue of
 * the backdrop toward that colour. Returns the receiver unchanged when
 * [backdrop] is null.
 */
fun Modifier.glassPanel(
    backdrop: Backdrop?,
    shape: Shape = RoundedCornerShape(24.dp),
    fill: Brush =
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.16f)),
        ),
    blurRadius: Dp = 20.dp,
    lensHeight: Dp = 0.dp,
    lensAmount: Dp = 0.dp,
    hueTint: Color = Color.Unspecified,
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
                if (lensHeight > 0.dp) lens(lensHeight.toPx(), lensAmount.toPx())
            },
            onDrawSurface = {
                if (hueTint != Color.Unspecified) drawRect(hueTint, blendMode = BlendMode.Hue)
                drawRect(fill)
            },
        )
    }

/**
 * App-styled glass: the panel colour is the theme surface mixed with the
 * current primary (so it follows the playing track's cover), denser at the
 * top and lighter at the bottom, with a 1dp edge that catches light from the
 * top-left. With [liquid] set, the panel also refracts light at its edges and
 * shifts the backdrop hue toward the primary colour. No-op while
 * [LocalGlassBackdrop] is null.
 */
@Composable
fun Modifier.glassSurface(
    shape: Shape,
    accentMix: Float = 0.12f,
    blurRadius: Dp = 20.dp,
    liquid: Boolean = false,
    density: Float = 1f,
): Modifier {
    val backdrop = LocalGlassBackdrop.current ?: return this
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    val body = lerp(scheme.surface, scheme.primary, if (liquid) accentMix + 0.08f else accentMix)
    val topAlpha = if (liquid) (if (dark) 0.34f else 0.46f) else (if (dark) 0.46f else 0.58f)
    val bottomAlpha = if (liquid) (if (dark) 0.20f else 0.30f) else (if (dark) 0.30f else 0.40f)
    val fill =
        Brush.verticalGradient(
            listOf(
                body.copy(alpha = (topAlpha * density).coerceAtMost(0.96f)),
                body.copy(alpha = (bottomAlpha * density).coerceAtMost(0.96f)),
            ),
        )
    val rimStart = if (liquid) (if (dark) 0.55f else 0.85f) else (if (dark) 0.34f else 0.70f)
    val rimEnd = if (liquid) (if (dark) 0.22f else 0.40f) else (if (dark) 0.14f else 0.30f)
    val edge =
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = rimStart),
                Color.White.copy(alpha = 0.06f),
                Color.White.copy(alpha = rimEnd),
            ),
        )
    return this
        .glassPanel(
            backdrop = backdrop,
            shape = shape,
            fill = fill,
            blurRadius = if (liquid) minOf(blurRadius, 8.dp) else blurRadius,
            lensHeight = if (liquid) 14.dp else 0.dp,
            lensAmount = if (liquid) 28.dp else 0.dp,
            hueTint = if (liquid) scheme.primary else Color.Unspecified,
        ).border(width = 1.dp, brush = edge, shape = shape)
}
