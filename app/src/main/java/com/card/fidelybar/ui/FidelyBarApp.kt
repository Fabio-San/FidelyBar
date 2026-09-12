package com.card.fidelybar.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.card.fidelybar.FidelyBarViewModel
import com.card.fidelybar.ui.detail.CardDetailScreen
import com.card.fidelybar.ui.editor.EditorScreen
import com.card.fidelybar.ui.home.HomeScreen
import com.card.fidelybar.ui.settings.SettingsScreen

object Routes {
    const val HOME = "home"
    const val EDITOR = "editor"
    const val SETTINGS = "settings"
    const val CARD = "card/{cardId}"
    fun editor(cardId: String? = null) = if (cardId == null) EDITOR else "editor?cardId=$cardId"
    fun card(cardId: String) = "card/$cardId"
}

private val TransitionEase = FastOutSlowInEasing

private fun navEnter() = fadeIn(tween(200, easing = TransitionEase)) +
    slideInVertically(tween(260, easing = TransitionEase)) { it / 10 }

private fun navExit() = fadeOut(tween(130, easing = TransitionEase))

private fun navPopExit() = fadeOut(tween(150, easing = TransitionEase)) +
    slideOutVertically(tween(220, easing = TransitionEase)) { it / 3 }

@Composable
fun FidelyBarApp(viewModel: FidelyBarViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { navEnter() },
        exitTransition = { navExit() },
        popEnterTransition = { navEnter() },
        popExitTransition = { navPopExit() }
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onOpenCard = { id -> navController.navigate(Routes.card(id)) },
                onAddCard = { navController.navigate(Routes.editor()) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "editor?cardId={cardId}",
            arguments = listOf(
                navArgument("cardId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId")
            EditorScreen(
                viewModel = viewModel,
                cardId = cardId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(
            route = Routes.CARD,
            arguments = listOf(navArgument("cardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
            CardDetailScreen(
                viewModel = viewModel,
                cardId = cardId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.editor(id)) }
            )
        }
    }
}