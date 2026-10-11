package moe.rukamori.archivetune.ui.component

import androidx.compose.animation.core.CubicBezierEasing

/**
 * Heavy, decelerating curve for glass-era transitions: cubic-bezier(0.32, 0.72, 0, 1).
 * Starts fast and settles slowly, which reads as mass rather than a UI tween.
 */
val GlassEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
