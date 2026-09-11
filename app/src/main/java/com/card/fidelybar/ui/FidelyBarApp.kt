package com.card.fidelybar.ui

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

object Routes {
    const val HOME = "home"
    const val EDITOR = "editor"
    const val CARD = "card/{cardId}"
    fun editor(cardId: String? = null) = if (cardId == null) EDITOR else "editor?cardId=$cardId"
    fun card(cardId: String) = "card/$cardId"
}

@Composable
fun FidelyBarApp(viewModel: FidelyBarViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                viewModel = viewModel,
                onOpenCard = { id -> navController.navigate(Routes.card(id)) },
                onAddCard = { navController.navigate(Routes.editor()) }
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