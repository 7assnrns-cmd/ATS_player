/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.R

/**
 * Bottom sheet shown when the launch-time update check finds a newer
 * release. It surfaces the version number and a short preview of the
 * changelog, and offers two actions:
 *
 * - Go to updates: navigates to the update-detail screen where the user
 *   can pick an ABI-specific build and start the download.
 * - Later: closes the sheet without doing anything. The version is
 *   remembered by the caller so the sheet will not be shown again for the
 *   same release.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateFoundSheet(
    version: String,
    changelog: String?,
    onGoToUpdates: () -> Unit,
    onLater: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var answered by remember { mutableStateOf(false) }

    fun answer(action: () -> Unit) {
        if (answered) return
        answered = true
        action()
    }

    fun finish(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { answer(action) }
    }

    ModalBottomSheet(
        onDismissRequest = { answer(onLater) },
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Header — icon + title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.sync),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.update_found_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = version,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Changelog preview
            val trimmed = remember(changelog) { trimChangelog(changelog) }
            if (!trimmed.isNullOrBlank()) {
                Text(
                    text = trimmed,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    text = stringResource(R.string.update_found_no_notes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Actions
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { finish(onGoToUpdates) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 24.dp),
                ) {
                    Text(stringResource(R.string.update_found_go_to_updates))
                }
                TextButton(
                    onClick = { finish(onLater) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.update_found_later))
                }
            }
        }
    }
}

/**
 * Changelog bodies from GitHub are markdown and often start with many
 * headers and links. For the sheet we only need the first meaningful
 * block of text, so we drop leading blank lines, skip lines that are pure
 * markdown scaffolding (---, ###, etc.), and stop at the first empty line
 * after a paragraph has started.
 */
private fun trimChangelog(body: String?): String? {
    if (body.isNullOrBlank()) return null
    val lines = body.lines()
    val kept = StringBuilder()
    var paragraphStarted = false
    for (raw in lines) {
        val line = raw.trim()
        if (line.isEmpty()) {
            if (paragraphStarted) break
            continue
        }
        if (line.startsWith("---") || line.startsWith("***") || line.startsWith("___")) {
            if (paragraphStarted) break
            continue
        }
        kept.appendLine(line)
        paragraphStarted = true
    }
    val result = kept.toString().trim()
    return result.takeIf { it.isNotBlank() }
}
