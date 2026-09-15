package com.card.fidelybar.ui

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring

// Molle "Espressive" (stile Material 3 / Google Wallet):
// - spatial: un leggero overshoot che si assesta morbido
// - effects: niente rimbalzo, solo energia (fade/colore)
val WalletSpatial: AnimationSpec<Float> = spring(dampingRatio = 0.9f, stiffness = 620f)
val WalletEffects: AnimationSpec<Float> = spring(dampingRatio = 0.8f, stiffness = 900f)