package com.tillzo.pos.util

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier

/**
 * Universal bottom safe area padding
 * Use this on any full-screen composable that has
 * content near the bottom edge
 */
fun Modifier.safeBottomPadding(): Modifier = this
    .navigationBarsPadding()
    .imePadding() // Also handles keyboard popup
