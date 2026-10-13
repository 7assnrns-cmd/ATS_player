/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import moe.rukamori.archivetune.R

/**
 * Bottom sheet for picking the app language.
 * @param currentTag BCP-47 tag of the currently selected language ("system" for system default).
 * @param onDismiss Called when the user dismisses the sheet.
 * @param onSelect Called with the picked tag. The caller is responsible for persisting + applying.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagePickerSheet(
    currentTag: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val languages = remember { supportedLanguages() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Text(
                text = stringResource(R.string.app_language),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp),
            ) {
                items(languages, key = { it.first }) { (tag, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(tag) }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = tag == currentTag,
                            onClick = { onSelect(tag) },
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
            Spacer(Modifier.heightIn(min = 16.dp))
        }
    }
}

/** Display name for a language tag (used in the settings row). */
fun languageDisplayName(tag: String): String =
    supportedLanguages().firstOrNull { it.first == tag }?.second ?: tag

private fun supportedLanguages(): List<Pair<String, String>> = listOf(
    "system" to "System default",
    "ar" to "العربية",
    "ar-EG" to "العربية (مصر)",
    "az" to "Azərbaycanca",
    "cs" to "Čeština",
    "de" to "Deutsch",
    "es" to "Español",
    "et" to "Eesti",
    "fa" to "فارسی",
    "fr" to "Français",
    "hi" to "हिन्दी",
    "hu" to "Magyar",
    "in" to "Bahasa Indonesia",
    "it" to "Italiano",
    "iw" to "עברית",
    "ja" to "日本語",
    "jv" to "Basa Jawa",
    "ko" to "한국어",
    "nl" to "Nederlands",
    "pl" to "Polski",
    "pt" to "Português",
    "pt-BR" to "Português (Brasil)",
    "ru" to "Русский",
    "si" to "සිංහල",
    "sl" to "Slovenščina",
    "ta" to "தமிழ்",
    "th" to "ไทย",
    "tr" to "Türkçe",
    "uk" to "Українська",
    "ur" to "اردو",
    "uz" to "Oʻzbekcha",
    "vi" to "Tiếng Việt",
    "zh-CN" to "简体中文",
    "zh-TW" to "繁體中文",
)
