package moe.rukamori.archivetune.ui.component

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

/**
 * Blurs everything behind the window this composable is hosted in (a dialog or
 * a sheet) using the system's cross-window blur. Needs Android 12 or newer and a
 * device that allows window blur; otherwise it silently does nothing. It is also
 * a no-op while the glass effect is off.
 */
@Composable
fun GlassWindowBlur(radius: Dp = 28.dp) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    if (LocalGlassBackdrop.current == null) return
    val view = LocalView.current
    val radiusPx = with(LocalDensity.current) { radius.roundToPx() }
    SideEffect {
        val provider = view.parent as? DialogWindowProvider
        if (provider != null) {
            val window = provider.window
            window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
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
