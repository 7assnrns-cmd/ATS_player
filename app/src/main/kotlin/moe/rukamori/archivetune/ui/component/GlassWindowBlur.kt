package moe.rukamori.archivetune.ui.component

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import moe.rukamori.archivetune.constants.GlassBlurStrengthKey
import moe.rukamori.archivetune.utils.rememberEnumPreference

/**
 * Blurs everything behind the window this composable is hosted in (a dialog
 * or sheet) using the system cross-window blur (Android 12+).
 *
 * The blur radius animates smoothly from 0 → target over 420ms with
 * FastOutSlowInEasing, so sheets/dialogs fade in naturally.
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
    val density = LocalDensity.current
    val effectiveDim = dimAmount ?: strength.dim
    val targetRadiusPx = with(density) { (radius ?: strength.radiusDp.dp).roundToPx() }

    val blurAnim = remember { Animatable(0f) }
    val dimAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        blurAnim.animateTo(
            targetValue = targetRadiusPx.toFloat(),
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        )
    }
    LaunchedEffect(Unit) {
        dimAnim.animateTo(
            targetValue = effectiveDim,
            animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        )
    }

    val radiusNow = blurAnim.value.toInt()
    val dimNow = dimAnim.value

    SideEffect {
        val provider = view.parent as? DialogWindowProvider
        if (provider != null) {
            val window = provider.window
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(dimNow)
            val attrs = window.attributes
            if (attrs.blurBehindRadius != radiusNow) {
                attrs.blurBehindRadius = radiusNow
                window.attributes = attrs
            }
        } else {
            val root = view.rootView
            if (root === (view.context as? Activity)?.window?.decorView) return@SideEffect
            val params = root.layoutParams as? WindowManager.LayoutParams ?: return@SideEffect
            if (params.blurBehindRadius == radiusNow &&
                (params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND) != 0) return@SideEffect
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            params.blurBehindRadius = radiusNow
            root.context
                .getSystemService(WindowManager::class.java)
                .updateViewLayout(root, params)
        }
    }
}

@Composable
fun glassDialogContainer(): Color =
    if (LocalGlassBackdrop.current != null) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.62f)
    } else {
        AlertDialogDefaults.containerColor
    }

@Composable
fun glassSheetContainer(): Color =
    if (LocalGlassBackdrop.current != null) {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.66f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

enum class GlassBlurStrength(
    val radiusDp: Int,
    val dim: Float,
) {
    LIGHT(12, 0.10f),
    MEDIUM(24, 0.16f),
    STRONG(44, 0.22f),
}
