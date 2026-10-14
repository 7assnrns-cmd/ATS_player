package moe.rukamori.archivetune.ui.component

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import moe.rukamori.archivetune.constants.GlassBlurStrengthKey
import moe.rukamori.archivetune.utils.rememberEnumPreference

/**
 * Blurs everything behind the window this composable is hosted in (a dialog or
 * a sheet) using the system's cross-window blur. Needs Android 12 or newer and a
 * device that allows window blur; otherwise it silently does nothing. It is also
 * a no-op while the glass effect is off.
 *
 * The blur radius is animated from 0 → target over 320ms so sheets and dialogs
 * fade in smoothly instead of "snapping" the blur into place.
 */
@Composable
fun GlassWindowBlur(
    radius: Dp? = null,
    dimAmount: Float? = null,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    if (LocalGlassBackdrop.current == null) return
    val view = LocalView.current
    val strength by rememberEnumPreference(GlassBlurStrengthKey, defaultValue = GlassBlurStrength.LIGHT)
    val effectiveDim = dimAmount ?: strength.dim
    val targetRadiusPx = with(LocalDensity.current) { (radius ?: strength.radiusDp.dp).roundToPx() }

    // Trigger: animate 0 → target on first composition (window appears)
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    // Animate the blur radius. When `appeared` becomes true the state flows from
    // 0 to targetRadiusPx over ~320ms; each recomposition applies the new value
    // to the window attributes (see SideEffect below).
    val animatedRadiusPx by animateIntAsState(
        targetValue = if (appeared) targetRadiusPx else 0,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "glassWindowBlurRadius",
    )

    // Animate the dim as well for a smoother fade
    val targetDim = if (appeared) effectiveDim else 0f
    val animatedDim by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetDim,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "glassWindowBlurDim",
    )

    SideEffect {
        val provider = view.parent as? DialogWindowProvider
        if (provider != null) {
            val window = provider.window
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(animatedDim)
            val attrs = window.attributes
            if (attrs.blurBehindRadius != animatedRadiusPx) {
                attrs.blurBehindRadius = animatedRadiusPx
                window.attributes = attrs
            }
        } else {
            val root = view.rootView
            // Never touch the activity's own window.
            if (root === (view.context as? Activity)?.window?.decorView) return@SideEffect
            val params = root.layoutParams as? WindowManager.LayoutParams ?: return@SideEffect
            val alreadyApplied =
                params.blurBehindRadius == animatedRadiusPx &&
                    (params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND) != 0
            if (alreadyApplied) return@SideEffect
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            params.blurBehindRadius = animatedRadiusPx
            root.context
                .getSystemService(WindowManager::class.java)
                .updateViewLayout(root, params)
        }
    }
}

/** Dialog container: translucent while glass is on, so the blurred window behind shows through. */
@Composable
fun glassDialogContainer(): Color =
    if (LocalGlassBackdrop.current != null) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.62f)
    } else {
        AlertDialogDefaults.containerColor
    }

/** Modal sheet container: translucent while glass is on. */
@Composable
fun glassSheetContainer(): Color =
    if (LocalGlassBackdrop.current != null) {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.66f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

/** How strongly the app behind a dialog or sheet is blurred and dimmed. */
enum class GlassBlurStrength(
    val radiusDp: Int,
    val dim: Float,
) {
    LIGHT(12, 0.10f),
    MEDIUM(24, 0.16f),
    STRONG(44, 0.22f),
}
