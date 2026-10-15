/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.updates

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.rukamori.archivetune.utils.Updater
import timber.log.Timber

/**
 * Pre-fetches and pre-translates the GitHub commit history so the update
 * screen always shows localized text — no flash of English before the
 * background translation catches up.
 *
 * Called on app startup. Cheap on subsequent runs because every (lang, sha)
 * pair is cached in TranslationCache and skips the network entirely.
 *
 * Safe to call repeatedly — concurrent calls are serialized by an internal
 * Mutex so the app never runs duplicate translation bursts.
 */
object ChangelogPrefetcher {

    private val mutex = kotlinx.coroutines.sync.Mutex()
    private var lastRunAt: Long = 0L
    private const val MIN_INTERVAL_MS = 60_000L // 1 minute

    /**
     * Fetch the latest 30 commits and translate any that are missing from
     * the cache. Returns immediately if the prefetcher ran less than
     * [MIN_INTERVAL_MS] ago — protected by a mutex.
     */
    suspend fun prefetch(context: Context) {
        if (!mutex.tryLock()) {
            Timber.d("ChangelogPrefetcher: already running, skipping")
            return
        }
        try {
            val now = System.currentTimeMillis()
            if (now - lastRunAt < MIN_INTERVAL_MS) {
                Timber.d("ChangelogPrefetcher: throttled, skipping")
                return
            }
            lastRunAt = now

            val targetLang = TranslationService.currentTargetLanguage() ?: return
            Timber.d("ChangelogPrefetcher: target language = $targetLang")

            val commits = withContext(Dispatchers.IO) {
                Updater.getCommitHistory(30).getOrNull().orEmpty()
            }
            if (commits.isEmpty()) {
                Timber.d("ChangelogPrefetcher: no commits")
                return
            }

            var translated = 0
            var skipped = 0
            for (commit in commits) {
                val cacheKey = commit.sha.ifBlank { commit.url }
                val cached = TranslationCache.get(context, targetLang, cacheKey)
                if (cached != null) {
                    skipped++
                    continue
                }
                val result = TranslationService.translate(commit.message, targetLang)
                if (result != null) {
                    TranslationCache.put(context, targetLang, cacheKey, result)
                    translated++
                }
            }
            Timber.d(
                "ChangelogPrefetcher: done — translated=$translated, cached=$skipped, total=${commits.size}",
            )
        } catch (e: Exception) {
            Timber.w(e, "ChangelogPrefetcher failed")
        } finally {
            mutex.unlock()
        }
    }
}
