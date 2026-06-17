package com.fizzycoyote.qusetroll.main.ui

import androidx.compose.ui.platform.ComposeView
import com.fizzycoyote.qusetroll.core.ui.ConceptBackground

fun setComposeBackground(composeView: ComposeView) {
    composeView.setContent {
        ConceptBackground()
    }
}