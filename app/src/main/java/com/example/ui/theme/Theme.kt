package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ClassicIvoryColorScheme = lightColorScheme(
    primary = Color(0xFF800020),            // Deep Burgundy
    primaryContainer = Color(0xFFFFDAD9),   // Pearly Soft Burgundy
    onPrimaryContainer = Color(0xFF41000A), // Intense Dark Burgundy
    secondary = Color(0xFF8C6E12),          // Amber Brass
    secondaryContainer = Color(0xFFFFF1C6), // Warm Straw Gold
    onSecondaryContainer = Color(0xFF2C1F00), // Roasted Coffee Brown
    tertiary = Color(0xFF5D4037),           // Oak Wood Accent
    tertiaryContainer = Color(0xFFE7D1C9),  // Soft Clay Linen
    background = Color(0xFFFAF8F5),         // Lustrous Parchment White
    surface = Color(0xFFFDFCF9),            // Warm Ivory Surface
    surfaceVariant = Color(0xFFF5EFEB),     // Cream Linen Variant
    onSurfaceVariant = Color(0xFF53433F),   // Dark Sepia Slate
    outline = Color(0xFF85736E),            // Dappled Bronze line
    outlineVariant = Color(0xFFDCCDC7),     // Soft Cashmere outline
    onPrimary = Color.White,
    onSecondary = Color(0xFF2C1E14),
    onTertiary = Color.White,
    onBackground = Color(0xFF2A1F1B),       // Dark Roasted Espresso
    onSurface = Color(0xFF2A1F1B),
    errorContainer = AlertRedBg,
    onErrorContainer = AlertRedText
)

private val ClassicNavyColorScheme = lightColorScheme(
    primary = Color(0xFF0F1E36),            // Royal Oxford Blue
    primaryContainer = Color(0xFFD8E2FF),   // Sky Cobalt Blue
    onPrimaryContainer = Color(0xFF001A41), // Deep Midnight Void
    secondary = Color(0xFF8F7A50),          // Burnished Muted Gold
    secondaryContainer = Color(0xFFFFF0D4), // Warm Straw Silk
    onSecondaryContainer = Color(0xFF2A1900), // Deep Amber Chocolate
    tertiary = Color(0xFF435B7E),           // Slate Steel Blue
    tertiaryContainer = Color(0xFFD7E2FF),  // Powder Blue Glass
    background = Color(0xFFF5F7FA),         // Chilled Alabaster White
    surface = Color.White,                  // Pure Snowy Alabaster Surface
    surfaceVariant = Color(0xFFE2E7F3),     // Soft Slate Gray Variant
    onSurfaceVariant = Color(0xFF44474F),   // Dusk Charcoal
    outline = Color(0xFF74777F),            // Deep Mist Outline
    outlineVariant = Color(0xFFC4C6CF),     // Soft Slate Grey Line
    onPrimary = Color.White,
    onSecondary = Color(0xFF0F1E36),
    onTertiary = Color.White,
    onBackground = Color(0xFF101C2B),       // Imperial Blue Raven
    onSurface = Color(0xFF101C2B),
    errorContainer = AlertRedBg,
    onErrorContainer = AlertRedText
)

private val ClassicForestColorScheme = lightColorScheme(
    primary = Color(0xFF1C4221),            // Sylvan Academic Green
    primaryContainer = Color(0xFFCEFFD1),   // Young Olive Lime
    onPrimaryContainer = Color(0xFF002206), // Deep Forest Obsidian
    secondary = Color(0xFF7E5225),          // Mahogany Wood Amber
    secondaryContainer = Color(0xFFFFDDBB), // Warm Pine Wood Spice
    onSecondaryContainer = Color(0xFF2B1200), // Deep Mahogany
    tertiary = Color(0xFF4C663C),           // Herb Olive Leaf
    tertiaryContainer = Color(0xFFCDECB5),  // Soft Sage Meadow
    background = Color(0xFFF6F8F3),         // Soft Pine Bud Alabaster
    surface = Color.White,
    surfaceVariant = Color(0xFFE1E5DC),     // Earthy Sage Grey
    onSurfaceVariant = Color(0xFF43483F),   // Dark Herb Cedar
    outline = Color(0xFF73796E),            // Dull Sage Mist
    outlineVariant = Color(0xFFC3C8BC),     // Light Herb Dust
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1A1F16),       // Black Spruce Obsidian
    onSurface = Color(0xFF1A1F16),
    errorContainer = AlertRedBg,
    onErrorContainer = AlertRedText
)

private val ClassicMidnightColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),            // Glowing Starlight Cyan
    primaryContainer = Color(0xFF004E72),   // Ocean Depth Blue
    onPrimaryContainer = Color(0xFFE0F2FE), // Cool Pale Aqua
    secondary = Color(0xFFFCD34D),          // Amber Star Dust
    secondaryContainer = Color(0xFF78350F), // Golden Honey Ember
    onSecondaryContainer = Color(0xFFFEF3C7), // Soft Sunlit Honey
    tertiary = Color(0xFF4ADE80),           // Aura Aurora Green
    tertiaryContainer = Color(0xFF052E16),  // Deep Emerald Glade
    background = Color(0xFF0B132B),         // Deep Astral Abyss Black
    surface = Color(0xFF1C2541),            // Cosmic Midnight Indigo
    surfaceVariant = Color(0xFF3A506B),     // Iridescent Meteorite Blue
    onSurfaceVariant = Color(0xFFE2E8F0),   // Cosmic Dust Grey
    outline = Color(0xFF5C6F8E),            // Astral Nebula Edge
    outlineVariant = Color(0xFF334155),     // Velvet Slate Outline
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color(0xFF0F172A),
    onTertiary = Color(0xFF0F172A),
    onBackground = Color(0xFFF1F5F9),       // Galactic Star Alabaster
    onSurface = Color(0xFFF1F5F9),
    errorContainer = AlertRedBg,
    onErrorContainer = AlertRedText
)

@Composable
fun MyApplicationTheme(
    themeMode: String = "CLASSIC_IVORY",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        "CLASSIC_IVORY" -> ClassicIvoryColorScheme
        "CLASSIC_NAVY" -> ClassicNavyColorScheme
        "CLASSIC_FOREST" -> ClassicForestColorScheme
        "CLASSIC_MIDNIGHT" -> ClassicMidnightColorScheme
        else -> if (darkTheme) ClassicMidnightColorScheme else ClassicIvoryColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
