/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.updates

import android.util.Log
import java.net.URLEncoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import timber.log.Timber

/**
 * Free translation service using the Google Translate web endpoint.
 *
 * This is used to translate release notes / commit messages into the user's
 * app language in the background, so the update screen always shows text in
 * the language the user already speaks — no flash of foreign content.
 *
 * Not affiliated with Google. Respect their usage limits and cache results
 * aggressively (see TranslationCache).
 */
object TranslationService {

    private val client = OkHttpClient.Builder().build()

    /**
     * Returns the BCP-47 tag of the app's current language, or null when the
     * language is already English (no translation needed).
     */
    fun currentTargetLanguage(): String? {
        val locale = Locale.getDefault()
        val lang = locale.language.lowercase()
        return when {
            lang.isBlank() -> null
            lang == "en" -> null
            else -> lang
        }
    }

    /**
     * Translates [text] to [targetLanguage]. Returns null on any error — the
     * caller should fall back to the original text.
     *
     * The endpoint accepts multiple q= parameters, but for simplicity we send
     * one string at a time. Batch translation happens at the worker level.
     */
    suspend fun translate(
        text: String,
        targetLanguage: String,
    ): String? = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext text
        if (targetLanguage.equals("en", ignoreCase = true)) return@withContext text

        try {
            val encoded = URLEncoder.encode(text, "UTF-8")
            val url =
                "https://translate.googleapis.com/translate_a/single" +
                    "?client=gtx&sl=auto&tl=$targetLanguage&dt=t&q=$encoded"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ArchiveTune-Translation/1.0")
                .build()

            val response = client.newCall(request).execute()
            response.use { resp ->
                if (!resp.isSuccessful) {
                    Timber.w("Translation HTTP ${resp.code}")
                    return@withContext null
                }
                val body = resp.body?.string() ?: return@withContext null
                val json = JSONArray(body)
                val segments = json.optJSONArray(0) ?: return@withContext null
                val sb = StringBuilder()
                for (i in 0 until segments.length()) {
                    val seg = segments.optJSONArray(i) ?: continue
                    val translated = seg.optString(0, "")
                    sb.append(translated)
                }
                sb.toString().ifBlank { null }
            }
        } catch (e: Exception) {
            Timber.w(e, "Translation failed")
            null
        }
    }
}
