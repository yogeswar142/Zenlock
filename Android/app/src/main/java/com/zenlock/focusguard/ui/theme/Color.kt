package com.zenlock.focusguard.ui.theme

import androidx.compose.ui.graphics.Color

// ================================================================
// Zenlock "Digital Sanctuary" Design System – Color Tokens
// Anchored in obsidian navy with a restrained violet accent.
// ================================================================

// ── Core Brand ──
val ZenPrimary          = Color(0xFFCEBDFF)   // primary (light lavender text)
val ZenPrimaryContainer = Color(0xFF9D7BFF)   // primary-container (accent fills)
val ZenOnPrimary        = Color(0xFF390094)
val ZenOnPrimaryContainer = Color(0xFF320085)
val ZenInversePrimary   = Color(0xFF6844C7)

// ── Surface Tiers ──
val ZenBackground       = Color(0xFF0A0E17)   // deep obsidian navy
val ZenSurface          = Color(0xFF14121A)
val ZenSurfaceDim       = Color(0xFF14121A)
val ZenSurfaceContainerLowest = Color(0xFF0F0D15)
val ZenSurfaceContainerLow    = Color(0xFF1D1A22)
val ZenSurfaceContainer       = Color(0xFF211E26)
val ZenSurfaceContainerHigh   = Color(0xFF2B2931)
val ZenSurfaceContainerHighest = Color(0xFF36333C)
val ZenSurfaceBright    = Color(0xFF3B3841)
val ZenSurfaceVariant   = Color(0xFF36333C)
val ZenCard             = Color(0xFF1F2937)  // glass-card background

// ── On-surface ──
val ZenOnSurface        = Color(0xFFE6E0EC)
val ZenOnSurfaceVariant = Color(0xFFCBC3D5)
val ZenOnBackground     = Color(0xFFE6E0EC)

// ── Outline ──
val ZenOutline          = Color(0xFF948E9F)
val ZenOutlineVariant   = Color(0xFF494553)

// ── Secondary ──
val ZenSecondary        = Color(0xFFC6C6C7)
val ZenSecondaryContainer = Color(0xFF454747)

// ── Tertiary ──
val ZenTertiary         = Color(0xFFBDC7D9)
val ZenTertiaryContainer = Color(0xFF8892A3)

// ── Error / Feedback ──
val ZenError            = Color(0xFFFFB4AB)
val ZenErrorContainer   = Color(0xFF93000A)
val ZenOnError          = Color(0xFF690005)

// ── Semantic accents (kept for compatibility with old code) ──
val Purple80  = ZenPrimary
val Purple60  = ZenPrimaryContainer
val Purple40  = Color(0xFF5C4DB7)
val PurpleDark = Color(0xFF3A2D96)

val Cyan      = Color(0xFF64FFDA)
val CyanDark  = Color(0xFF00BFA5)
val Coral     = Color(0xFFFF6B6B)
val Amber     = Color(0xFFFFD54F)
val Emerald   = Color(0xFF66BB6A)

// Legacy aliases kept so nothing breaks
val DarkBg             = ZenBackground
val DarkBgSecondary    = Color(0xFF1A1A2E)
val DarkSurface        = ZenSurface
val DarkSurfaceVariant = ZenSurfaceContainerLow
val DarkCard           = ZenCard

val TextPrimary   = ZenOnSurface
val TextSecondary = ZenOnSurfaceVariant
val TextMuted     = ZenOutline

val SuccessGreen  = Color(0xFF4CAF50)
val WarningOrange = Color(0xFFFF9800)
val ErrorRed      = Color(0xFFFF5252)
val InfoBlue      = Color(0xFF2196F3)
