/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

fun reportException(throwable: Throwable) {
    throwable.printStackTrace()
}

@Suppress("DEPRECATION")
fun setAppLocale(
    context: Context,
    locale: Locale,
) {
    val tag = if (locale == Locale.getDefault() && locale.language == "en") "system" else locale.toLanguageTag()
    AppCompatDelegate.setApplicationLocales(
        LocaleListCompat.forLanguageTags(tag)
    )
}

fun setAppLocaleTag(
    context: Context,
    tag: String,
) {
    AppCompatDelegate.setApplicationLocales(
        if (tag == "system") LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(tag)
    )
}
