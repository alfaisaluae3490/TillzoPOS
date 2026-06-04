package com.tillzo.pos.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Primary Palette ───────────────────────────────────────────────────────
val BackgroundDark    = Color(0xFF1A1A1A)   // Main background — near-black
val SurfaceDark       = Color(0xFF2A2A2A)   // Cards, sheets, elevated surfaces
val SurfaceVariant    = Color(0xFF333333)   // Input fields, secondary cards
val SurfaceHighlight  = Color(0xFF3D3D3D)   // Pressed/active states

// ─── Accent ─────────────────────────────────────────────────────────────────
val AccentBlue        = Color(0xFF1E88E5)   // Primary accent — buttons, highlights
val AccentBlueLight   = Color(0xFF42A5F5)   // Lighter accent — hover states
val AccentBlueDark    = Color(0xFF1565C0)   // Darker accent — pressed states

// ─── Text ───────────────────────────────────────────────────────────────────
val TextPrimary       = Color(0xFFFFFFFF)   // Main text — pure white
val TextSecondary     = Color(0xFFB0B0B0)   // Subtext, hints, labels
val TextDisabled      = Color(0xFF666666)   // Disabled state text

// ─── Semantic ───────────────────────────────────────────────────────────────
val SuccessGreen      = Color(0xFF4CAF50)   // Synced, success states
val WarningAmber      = Color(0xFFFF9800)   // Pending sync, countdown warnings
val ErrorRed          = Color(0xFFE53935)   // Errors, failed sync, root detection
val UdhaarOrange      = Color(0xFFFF6F00)   // Khata — credit given (udhaar)
val JamaGreen         = Color(0xFF00C853)   // Khata — payment received (jama)

// ─── Numpad ─────────────────────────────────────────────────────────────────
val NumpadKey         = Color(0xFF2A2A2A)   // Number key background
val NumpadKeyPressed  = Color(0xFF1E88E5)   // Number key — pressed state
val NumpadActionKey   = Color(0xFF1E88E5)   // Action keys (Pay, Clear)
val NumpadText        = Color(0xFFFFFFFF)   // Numpad digit text
