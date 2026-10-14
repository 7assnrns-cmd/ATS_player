/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * Returns the natural reading direction of [text] based on its first
 * strong-directional character.
 *
 * - Arabic / Hebrew / Persian / Urdu / Syriac / Thaana → RTL
 * - Latin / Cyrillic / Greek / CJK / Devanagari / etc. → LTR
 * - Digits, punctuation, whitespace at the start are skipped
 * - Falls back to [fallback] when no strong character is found
 *
 * This is the same algorithm Android's ICU library uses for bidi paragraph
 * detection, but limited to the first strong char which is what we want for
 * a title that may mix scripts.
 */
fun naturalDirectionOf(
    text: String,
    fallback: LayoutDirection = LayoutDirection.Ltr,
): LayoutDirection {
    for (c in text) {
        when (Character.getDirectionality(c)) {
            Character.DIRECTIONALITY_RIGHT_TO_LEFT,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
            Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE,
            -> return LayoutDirection.Rtl

            Character.DIRECTIONALITY_LEFT_TO_RIGHT,
            Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING,
            Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE,
            -> return LayoutDirection.Ltr
        }
    }
    return fallback
}

/**
 * Marquee modifier that scrolls overflowing text horizontally.
 *
 * The scroll direction is inherited from the ambient [LocalLayoutDirection],
 * which means if you're inside an RTL locale but the text is English, the
 * marquee will still scroll LTR. Use [MarqueeText] (below) to get automatic
 * per-string direction detection.
 */
fun Modifier.marqueeScroll(
    velocity: Dp = 30.dp,
    repeatDelayMillis: Int = 1500,
    initialDelayMillis: Int = 800,
    spacing: MarqueeSpacing = MarqueeSpacing(0.35f),
): Modifier =
    this.basicMarquee(
        iterations = Int.MAX_VALUE,
        animationMode = MarqueeAnimationMode.Immediately,
        repeatDelayMillis = repeatDelayMillis,
        initialDelayMillis = initialDelayMillis,
        spacing = spacing,
        velocity = velocity,
    )

/**
 * Drop-in replacement for [Text] that:
 *
 * 1. Auto-scrolls when the text is wider than its container.
 * 2. Detects the natural reading direction of the string itself — Arabic and
 *    Hebrew scroll right-to-left, everything else scrolls left-to-right —
 *    regardless of the app's current locale direction.
 *
 * This means an English song title inside the Arabic app will still scroll
 * in the way an English speaker expects, and an Arabic song title inside an
 * English app will scroll the way an Arabic speaker expects.
 */
@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    velocity: Dp = 30.dp,
    repeatDelayMillis: Int = 1500,
    initialDelayMillis: Int = 800,
    autoDetectDirection: Boolean = true,
) {
    val ambientDirection = LocalLayoutDirection.current
    val textDirection =
        if (autoDetectDirection) naturalDirectionOf(text, ambientDirection) else ambientDirection

    CompositionLocalProvider(LocalLayoutDirection provides textDirection) {
        Text(
            text = text,
            modifier = modifier.marqueeScroll(
                velocity = velocity,
                repeatDelayMillis = repeatDelayMillis,
                initialDelayMillis = initialDelayMillis,
            ),
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            style = style,
        )
    }
}

/**
 * AnnotatedString overload of [MarqueeText].
 */
@Composable
fun MarqueeText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    style: TextStyle = LocalTextStyle.current,
    velocity: Dp = 30.dp,
    repeatDelayMillis: Int = 1500,
    initialDelayMillis: Int = 800,
    autoDetectDirection: Boolean = true,
) {
    val ambientDirection = LocalLayoutDirection.current
    val textDirection =
        if (autoDetectDirection) naturalDirectionOf(text.text, ambientDirection) else ambientDirection

    CompositionLocalProvider(LocalLayoutDirection provides textDirection) {
        Text(
            text = text,
            modifier = modifier.marqueeScroll(
                velocity = velocity,
                repeatDelayMillis = repeatDelayMillis,
                initialDelayMillis = initialDelayMillis,
            ),
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            minLines = minLines,
            style = style,
        )
    }
}
