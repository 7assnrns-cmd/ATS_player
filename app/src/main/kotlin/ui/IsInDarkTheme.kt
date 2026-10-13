/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * Small compatibility shim used by the Liquid Glass sample code imported
 * from the Miuix example tree. The upstream sample references `ui.isInDarkTheme`
 * but the file is not shipped in the library itself, so we provide it here.
 */
@Composable
internal fun isInDarkTheme(): Boolean = isSystemInDarkTheme()
