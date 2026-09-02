package com.example.locationtrackerapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Uriel Cafe brand palette.
 *
 * The app is light-mode-first: a near-white surface with dark, high-contrast
 * text and a single dark-red brand accent (matching the restaurant's logo
 * and app icon) used sparingly for primary actions and highlights.
 */

// Neutrals
val SurfaceWhite = Color(0xFFFDFBFA)
val BackgroundWhite = Color(0xFFF7F4F3)
val TextPrimary = Color(0xFF1A1414)
val TextSecondary = Color(0xFF7A7170)
val DividerColor = Color(0xFFECE6E5)
val SurfaceCard = Color(0xFFFFFFFF)

// Brand red (sampled from the restaurant's icon/logo artwork)
val BrandRed900 = Color(0xFF3E0407)
val BrandRed800 = Color(0xFF5C0709)
val BrandRed700 = Color(0xFF7A0F16)
val BrandRed600 = Color(0xFF931019)
val BrandRed500 = Color(0xFFAE1B22)
val BrandRedTint = Color(0xFFFBEBEC)

val CafeGold = BrandRed500
val LocationBlue = BrandRed700

// Feedback
val ErrorRed = Color(0xFFB3261E)
val ErrorRedContainer = Color(0xFFFBEBEC)

/** Subtle, premium diagonal gradient used for the header and primary buttons. */
val BrandRedGradient = Brush.linearGradient(
    colors = listOf(BrandRed900, BrandRed600)
)

val BrandRedGradientSoft = Brush.linearGradient(
    colors = listOf(BrandRed800, BrandRed500)
)
