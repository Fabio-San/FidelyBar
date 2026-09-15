package com.card.fidelybar.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Bottom sheet "Wallet" su misura, con la stessa identica molla in ingresso e
 * in uscita (al contrario della ModalBottomSheet di Material3, che ha un'uscita
 * "FastEffects" più secca).
 *
 * Il contenuto resta sempre composto (anche da chiuso): fa da pre-riscaldamento
 * e l'uscita è animata, non smontata di colpo.
 */
@Composable
fun FidelySheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    BackHandler(enabled = visible) { onDismiss() }

    val slideSpec: FiniteAnimationSpec<Float> = spring(dampingRatio = 0.9f, stiffness = 620f)
    val slideIntSpec: FiniteAnimationSpec<IntOffset> = spring(dampingRatio = 0.9f, stiffness = 620f)
    val fadeInSpec: FiniteAnimationSpec<Float> = spring(dampingRatio = 0.8f, stiffness = 900f)

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim: dissolvenza sul fondo; tap fuori per chiudere.
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = fadeInSpec),
            exit = fadeOut(animationSpec = slideSpec)
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.44f))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(animationSpec = slideIntSpec, initialOffsetY = { it }) +
                fadeIn(animationSpec = fadeInSpec),
            exit = slideOutVertically(animationSpec = slideIntSpec, targetOffsetY = { it }) +
                fadeOut(animationSpec = slideSpec),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp, bottom = 4.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                    content()
                }
            }
        }
    }
}

/**
 * Schermata a schermo intero (overlay) con la stessa molla "Wallet" in ingresso
 * e uscita del resto dell'app. Niente barra pull-up, niente angoli: le schermate
 * interne (Editor, Impostazioni) occupano tutto il display e gestiscono i propri
 * padding per status/navigation bar.
 *
 * Il contenuto resta sempre composto e disegnato (pre-riscaldamento): al primo
 * tocco su "Nuova carta" o Impostazioni la schermata è già pronta, niente lag da
 * cold-start. Quando chiusa, alpha va a 0 e translationY spinge il layer fuori
 * viewport così i tap passano a HomeScreen.
 */
@Composable
fun FullScreenHost(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    BackHandler(enabled = visible) { onDismiss() }

    val fadeInSpec: FiniteAnimationSpec<Float> = spring(dampingRatio = 0.8f, stiffness = 900f)
    val fadeOutSpec: FiniteAnimationSpec<Float> = spring(dampingRatio = 0.9f, stiffness = 620f)
    val scaleSpec: FiniteAnimationSpec<Float> = spring(dampingRatio = 0.82f, stiffness = 360f)

    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.97f) }

    LaunchedEffect(visible) {
        if (visible) {
            launch { alpha.animateTo(1f, fadeInSpec) }
            scale.animateTo(1f, scaleSpec)
        } else {
            launch { alpha.animateTo(0f, fadeOutSpec) }
            scale.animateTo(0.97f, scaleSpec)
        }
    }

    // Il contenuto resta sempre composto e disegnato, ma a schermo chiuso alpha
    // va a 0 e translationY spinge il layer fuori viewport: i tap passano così a
    // HomeScreen senza smontare (pre-riscaldamento) né ricomporre a ogni frame.
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    this.alpha = alpha.value
                    val s = scale.value
                    scaleX = s
                    scaleY = s
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    if (alpha.value < 0.01f) {
                        translationY = 200000f
                    } else {
                        translationY = 0f
                    }
                },
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}