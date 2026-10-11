package moe.rukamori.archivetune.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import moe.rukamori.archivetune.R

/**
 * One-time introduction to the glass look. Pressing either button, or
 * dismissing the sheet, counts as an answer; the caller stores it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassIntroSheet(
    onEnable: () -> Unit,
    onSkip: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var answered by remember { mutableStateOf(false) }

    // Report exactly one answer, whichever way the sheet is closed.
    fun answer(action: () -> Unit) {
        if (answered) return
        answered = true
        action()
    }

    // Slide the sheet away first, then report the answer.
    fun finish(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { answer(action) }
    }

    ModalBottomSheet(
        onDismissRequest = { answer(onSkip) },
        sheetState = sheetState,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            GlassBezel(modifier = Modifier.fillMaxWidth()) {
                GlassPreview()
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Eyebrow(text = stringResource(R.string.glass_intro_eyebrow))
                Text(
                    text = stringResource(R.string.glass_intro_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.glass_intro_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                InfoRow(
                    label = stringResource(R.string.glass_intro_where_label),
                    value = stringResource(R.string.glass_intro_where_value),
                )
                InfoRow(
                    label = stringResource(R.string.glass_intro_off_label),
                    value = stringResource(R.string.glass_intro_off_value),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = { finish(onEnable) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(start = 24.dp, end = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.glass_intro_enable),
                        modifier = Modifier.weight(1f),
                    )
                    // Trailing icon lives in its own circle, flush with the pill's inner padding.
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp).rotate(180f),
                        )
                    }
                }
                TextButton(
                    onClick = { finish(onSkip) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.glass_intro_skip))
                }
            }
        }
    }
}

/** Microscopic pill that sits above the headline. */
@Composable
private fun Eyebrow(text: String) {
    Box(
        modifier =
            Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

/** Decorative preview: three colour orbs on OLED black behind a small glass bar. */
@Composable
private fun GlassPreview() {
    val scheme = MaterialTheme.colorScheme
    val pill = RoundedCornerShape(28.dp)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(Color(0xFF050505))
                .drawBehind {
                    orb(scheme.primary, Offset(size.width * 0.20f, size.height * 0.40f), size.width * 0.40f)
                    orb(scheme.tertiary, Offset(size.width * 0.78f, size.height * 0.25f), size.width * 0.34f)
                    orb(scheme.secondary, Offset(size.width * 0.52f, size.height * 0.95f), size.width * 0.36f)
                },
    ) {
        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(pill)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.12f)),
                        ),
                    ).border(
                        width = 1.dp,
                        brush =
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.60f),
                                    Color.White.copy(alpha = 0.06f),
                                    Color.White.copy(alpha = 0.26f),
                                ),
                            ),
                        shape = pill,
                    ),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(4) { index ->
                Box(
                    modifier =
                        Modifier
                            .size(if (index == 0) 14.dp else 10.dp)
                            .clip(CircleShape)
                            .background(if (index == 0) Color.White else Color.White.copy(alpha = 0.55f)),
                )
            }
        }
    }
}

private fun DrawScope.orb(
    color: Color,
    center: Offset,
    radius: Float,
) {
    drawCircle(
        brush =
            Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.85f), Color.Transparent),
                center = center,
                radius = radius,
            ),
        radius = radius,
        center = center,
    )
}
