/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.updates

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import moe.rukamori.archivetune.utils.dataStore

/**
 * Persists translated changelog / commit messages keyed by
 * "<lang>:<sha>" so a commit is never translated twice.
 *
 * Keys are stored as a single JSON blob to keep DataStore writes cheap.
 */
object TranslationCache {

    private val CACHE_KEY = stringPreferencesKey("translation_cache_v1")

    private fun key(lang: String, id: String): String = "$lang:$id"

    suspend fun get(
        context: Context,
        lang: String,
        id: String,
    ): String? {
        val raw = context.dataStore.data.first()[CACHE_KEY] ?: return null
        return runCatching {
            val json = org.json.JSONObject(raw)
            json.optString(key(lang, id), "").ifBlank { null }
        }.getOrNull()
    }

    /**
     * Returns ALL cached translations for [lang] in one DataStore read.
     * Used to preload the entire map so the UI never flashes English.
     */
    suspend fun getAll(
        context: Context,
        lang: String,
    ): Map<String, String> {
        val raw = context.dataStore.data.first()[CACHE_KEY] ?: return emptyMap()
        return runCatching {
            val json = org.json.JSONObject(raw)
            val prefix = "$lang:"
            val result = mutableMapOf<String, String>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key.startsWith(prefix)) {
                    val sha = key.removePrefix(prefix)
                    result[sha] = json.optString(key, "")
                }
            }
            result
        }.getOrDefault(emptyMap())
    }

    suspend fun put(
        context: Context,
        lang: String,
        id: String,
        translation: String,
    ) {
        context.dataStore.edit { prefs ->
            val current = prefs[CACHE_KEY] ?: "{}"
            val json = runCatching { org.json.JSONObject(current) }.getOrElse { org.json.JSONObject() }
            json.put(key(lang, id), translation)
            prefs[CACHE_KEY] = json.toString()
        }
    }
}
