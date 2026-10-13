package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
 * Soft light catching the top of a glass surface. Drawn behind the content,
 * so it never lowers the contrast of text or icons placed on the glass.
 */
fun Modifier.glassSheen(
    shape: Shape,
    alpha: Float = 0.12f,
): Modifier =
    background(
        brush =
            Brush.verticalGradient(
                colorStops =
                    arrayOf(
                        0f to Color.White.copy(alpha = alpha),
                        0.5f to Color.Transparent,
                        1f to Color.Transparent,
                    ),
            ),
        shape = shape,
    )

/**
 * App-styled glass: the panel colour is the theme surface mixed with the
 * current primary (so it follows the playing track's cover), denser at the
 * top and lighter at the bottom, with a soft sheen along the top and, when
 * [rim] is set, a 1dp edge that catches light from the top-left. With
 * [liquid] set, the panel also refracts light at its edges and shifts the
 * backdrop hue toward the primary colour. [density] scales the panel opacity
 * (use above 1 where text must stay readable). No-op while
 * [LocalGlassBackdrop] is null.
 */
@Composable
fun Modifier.glassSurface(
    shape: Shape,
    accentMix: Float = 0.12f,
    blurRadius: Dp = 20.dp,
    liquid: Boolean = false,
    density: Float = 1f,
    rim: Boolean = true,
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
        ).glassSheen(shape = shape, alpha = if (liquid) 0.16f else 0.12f)
        .then(if (rim) Modifier.border(width = 1.dp, brush = edge, shape = shape) else Modifier)
}

/**
 * Double-bezel container: an outer shell with a hairline border holds an inner
 * core whose corner radius is the outer radius minus [inset], so both curves
 * stay concentric. The core fills the shell width and carries a top sheen.
 */
@Composable
fun GlassBezel(
    modifier: Modifier = Modifier,
    outerRadius: Dp = 28.dp,
    inset: Dp = 5.dp,
    innerFill: Brush? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val ink = if (dark) Color.White else Color.Black
    val outer = RoundedCornerShape(outerRadius)
    val inner = RoundedCornerShape((outerRadius - inset).coerceAtLeast(0.dp))
    Box(
        modifier =
            modifier
                .clip(outer)
                .background(ink.copy(alpha = 0.06f))
                .border(width = 1.dp, color = ink.copy(alpha = 0.10f), shape = outer)
                .padding(inset),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(inner)
                    .then(if (innerFill != null) Modifier.background(innerFill) else Modifier)
                    .glassSheen(shape = inner, alpha = 0.14f),
            content = content,
        )
    }
}


/** True inside the main top bar; pressable icons there get a soft contrast halo while glass is on. */
val LocalGlassBarHalo = staticCompositionLocalOf { false }

/** Marks its content as part of a glass top bar. */
@Composable
fun GlassBarHaloScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalGlassBarHalo provides true, content = content)
}

/** Hairline rim that makes a card read as a glass pane. Null while glass is off. */
@Composable
fun glassCardBorder(): BorderStroke? {
    if (LocalGlassBackdrop.current == null) return null
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val rim =
        if (dark) {
            listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.04f), Color.White.copy(alpha = 0.12f))
        } else {
            listOf(Color.White.copy(alpha = 0.95f), Color.Black.copy(alpha = 0.06f), Color.White.copy(alpha = 0.50f))
        }
    return BorderStroke(width = 1.dp, brush = Brush.linearGradient(rim))
}

/**
 * Header scrim for the main top bar: frosted glass while glass is on, the old
 * fade-out gradient ([fallback]) otherwise.
 */
@Composable
fun Modifier.glassHeaderScrim(fallback: Brush): Modifier {
    val backdrop = LocalGlassBackdrop.current ?: return this.background(fallback)
    val surface = MaterialTheme.colorScheme.surface
    return this
        .glassPanel(
            backdrop = backdrop,
            shape = RectangleShape,
            fill = Brush.verticalGradient(listOf(surface.copy(alpha = 0.62f), surface.copy(alpha = 0.34f))),
            blurRadius = 18.dp,
        ).glassSheen(shape = RectangleShape, alpha = 0.10f)
        .drawBehind {
            val line = 1.dp.toPx()
            drawRect(
                color = Color.White.copy(alpha = 0.12f),
                topLeft = Offset(0f, size.height - line),
                size = Size(size.width, line),
            )
        }
}


/**
 * Glass edge that follows [shape]; no-op while glass is off. [fadeTop] and
 * [fadeBottom] (0..1) fade the rim out along that edge, so two stacked glass
 * panels (mini player over navigation bar) share one clean seam.
 */
@Composable
fun Modifier.glassRim(
    shape: Shape,
    fadeTop: Float = 0f,
    fadeBottom: Float = 0f,
): Modifier {
    if (LocalGlassBackdrop.current == null) return this
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val top = (if (dark) 0.34f else 0.70f) * (1f - fadeTop.coerceIn(0f, 1f))
    val bottom = (if (dark) 0.14f else 0.30f) * (1f - fadeBottom.coerceIn(0f, 1f))
    val edge =
        Brush.verticalGradient(
            colorStops =
                arrayOf(
                    0f to Color.White.copy(alpha = top),
                    0.5f to Color.White.copy(alpha = 0.06f),
                    1f to Color.White.copy(alpha = bottom),
                ),
        )
    return this.border(width = 1.dp, brush = edge, shape = shape)
}

/** [base] made translucent while glass is on; unchanged otherwise. */
@Composable
fun glassFill(
    base: Color,
    alpha: Float = 0.62f,
): Color = if (LocalGlassBackdrop.current != null) base.copy(alpha = alpha) else base
