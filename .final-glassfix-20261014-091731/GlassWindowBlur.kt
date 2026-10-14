package moe.rukamori.archivetune.ui.component

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
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
    val radiusPx = with(LocalDensity.current) { (radius ?: strength.radiusDp.dp).roundToPx() }
    SideEffect {
        val provider = view.parent as? DialogWindowProvider
        if (provider != null) {
            val window = provider.window
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.setDimAmount(effectiveDim)
            val attrs = window.attributes
            if (attrs.blurBehindRadius != radiusPx) {
                attrs.blurBehindRadius = radiusPx
                window.attributes = attrs
            }
        } else {
            val root = view.rootView
            // Never touch the activity's own window.
            if (root === (view.context as? Activity)?.window?.decorView) return@SideEffect
            val params = root.layoutParams as? WindowManager.LayoutParams ?: return@SideEffect
            val applied =
                params.blurBehindRadius == radiusPx &&
                    (params.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND) != 0
            if (applied) return@SideEffect
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
            params.blurBehindRadius = radiusPx
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
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f)
    } else {
        AlertDialogDefaults.containerColor
    }

/** Modal sheet container: translucent while glass is on. */
@Composable
fun glassSheetContainer(): Color =
    if (LocalGlassBackdrop.current != null) {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.80f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }


/** How strongly the app behind a dialog or sheet is blurred and dimmed. */
enum class GlassBlurStrength(
    val radiusDp: Int,
    val dim: Float,
) {
    LIGHT(6, 0.12f),
    MEDIUM(14, 0.18f),
    STRONG(28, 0.24f),
}
