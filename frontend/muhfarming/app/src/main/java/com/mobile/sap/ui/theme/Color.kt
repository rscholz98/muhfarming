package com.mobile.sap.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Muhfarming design system — a fresh, agriculture-inspired palette built from
 * scratch (no vendor UI kit). Leaf-green primary, olive secondary, and a warm
 * harvest-amber accent, with neutral surfaces tuned for both light and dark.
 *
 * Screens should prefer `MaterialTheme.colorScheme.*` where possible; the named
 * constants below are for the few places that need a specific brand hue
 * (gradients, chips, status dots) that isn't a semantic scheme role.
 */

// ---- Brand core ----
val Leaf = Color(0xFF2E7D46)          // primary — leaf green
val LeafDark = Color(0xFF1E5A31)      // pressed / dark primary
val LeafLight = Color(0xFF5FA974)     // light primary (dark theme primary)
val Olive = Color(0xFF6D8B3A)         // secondary — olive
val OliveLight = Color(0xFF9BBE63)
val Harvest = Color(0xFFC9781F)       // tertiary — harvest amber
val HarvestLight = Color(0xFFE3A24E)

// Soft brand tints for containers / gradients / chip backgrounds.
val LeafTint = Color(0xFFDCEEDF)      // primaryContainer (light)
val LeafTintDark = Color(0xFF20351F)  // primaryContainer (dark)
val HarvestTint = Color(0xFFF7E4CC)

// Additional container roles.
val Color_OliveContainer = Color(0xFFE4EECB)
val Color_OliveOnContainer = Color(0xFF33420F)
val Color_HarvestOnContainer = Color(0xFF4A3211)
val Color_Scrim = Color(0x99000000)

// ---- Neutrals ----
val Sand = Color(0xFFF7F9F4)          // app background (light) — barely-green off-white
val Cloud = Color(0xFFFFFFFF)         // surface (light)
val Mist = Color(0xFFEDF1E9)          // surfaceVariant / dividers (light)
val Stone = Color(0xFFE2E7DC)         // outline (light)
val Slate = Color(0xFF5B6157)         // muted text (light)
val Ink = Color(0xFF1B2019)           // primary text (light)

// Dark theme neutrals.
val NightBg = Color(0xFF101510)       // background (dark)
val NightSurface = Color(0xFF1B211A)  // surface (dark)
val NightSurfaceHi = Color(0xFF242B22)// elevated surface (dark)
val NightVariant = Color(0xFF2A322A)  // surfaceVariant (dark)
val NightOutline = Color(0xFF3A433A)  // outline (dark)
val Fog = Color(0xFFB9C2B4)           // muted text (dark)
val Snow = Color(0xFFEDF2E9)          // primary text (dark)

// ---- Status ----
val Success = Color(0xFF2E7D46)
val Warning = Color(0xFFE08A00)
val Danger = Color(0xFFC0392B)
val Info = Color(0xFF2F80B8)

// Priority / severity accents (used by alerts & risks).
val SeverityHigh = Color(0xFFC0392B)
val SeverityMedium = Color(0xFFE08A00)
val SeverityLow = Color(0xFFB59A00)
