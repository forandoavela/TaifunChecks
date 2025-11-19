package com.taifun.checks.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.taifun.checks.ui.screens.*

object Routes {
    const val FIRST_LAUNCH = "first_launch"
    const val HOME = "home"
    const val EDITOR = "editor"
    const val STEP = "step"
    const val SETTINGS = "settings"
    const val HELP = "help"
    const val EDIT_CHECKLIST = "edit_checklist"
    const val MANAGER = "manager"
    const val LOG_VIEWER = "log_viewer"
    const val ARG_ID = "id"
    const val STEP_WITH_ARG = "$STEP/{$ARG_ID}"
    const val EDIT_CHECKLIST_WITH_ARG = "$EDIT_CHECKLIST/{$ARG_ID}"
}

@Composable
fun AppNavHost(
    nav: NavHostController,
    startDestination: String = Routes.HOME
) {
    NavHost(
        navController = nav,
        startDestination = startDestination
    ) {
        composable(Routes.FIRST_LAUNCH) {
            FirstLaunchScreen(
                onComplete = {
                    nav.navigate(Routes.HOME) {
                        // Limpiar el back stack
                        popUpTo(Routes.FIRST_LAUNCH) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenChecklist = { id -> nav.navigate("${Routes.STEP}/$id") },
                onOpenEditor = { nav.navigate(Routes.EDITOR) },
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onOpenHelp = { nav.navigate(Routes.HELP) },
                onEditChecklist = { id -> nav.navigate("${Routes.EDIT_CHECKLIST}/$id") },
                onOpenManager = { nav.navigate(Routes.MANAGER) },
                onOpenLogViewer = { nav.navigate(Routes.LOG_VIEWER) }
            )
        }

        composable(Routes.MANAGER) {
            ChecklistManagerScreen(
                onBack = { nav.popBackStack() },
                onEdit = { id -> nav.navigate("${Routes.EDIT_CHECKLIST}/$id") },
                onOpenEditor = { nav.navigate(Routes.EDITOR) }
            )
        }

        composable(Routes.EDITOR) {
            EditorScreen(
                onBack = {
                    nav.popBackStack()
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.HELP) {
            HelpScreen(
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.LOG_VIEWER) {
            LogViewerScreen(
                onBack = { nav.popBackStack() }
            )
        }

        composable(
            route = Routes.STEP_WITH_ARG,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val checklistId = backStackEntry.arguments?.getString(Routes.ARG_ID) ?: ""
            StepScreen(
                checklistId = checklistId,
                onBack = { nav.popBackStack() },
                onEdit = { id -> nav.navigate("${Routes.EDIT_CHECKLIST}/$id") }
            )
        }

        composable(
            route = Routes.EDIT_CHECKLIST_WITH_ARG,
            arguments = listOf(navArgument(Routes.ARG_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val checklistId = backStackEntry.arguments?.getString(Routes.ARG_ID) ?: ""
            EditChecklistScreen(
                checklistId = checklistId,
                onBack = {
                    nav.popBackStack()
                }
            )
        }
    }
}
