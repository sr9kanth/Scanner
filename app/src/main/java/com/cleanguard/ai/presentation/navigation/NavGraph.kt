package com.cleanguard.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cleanguard.ai.presentation.accessibility.AccessibilityScreen
import com.cleanguard.ai.presentation.apps.AppDetailScreen
import com.cleanguard.ai.presentation.apps.AppListScreen
import com.cleanguard.ai.presentation.chrome.ChromeScreen
import com.cleanguard.ai.presentation.dashboard.DashboardScreen
import com.cleanguard.ai.presentation.notifications.NotificationScreen
import com.cleanguard.ai.presentation.onboarding.OnboardingScreen
import com.cleanguard.ai.presentation.overlay.OverlayScreen
import com.cleanguard.ai.presentation.report.ReportScreen
import com.cleanguard.ai.presentation.scanner.ScannerScreen
import com.cleanguard.ai.presentation.screenshot.ScreenshotScreen
import com.cleanguard.ai.presentation.settings.SettingsScreen

@Composable
fun CleanGuardNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val startDest = remember {
        val prefs = context.getSharedPreferences("cleanguard_prefs", android.content.Context.MODE_PRIVATE)
        if (prefs.getBoolean("onboarding_complete", false)) Screen.Dashboard.route
        else Screen.Onboarding.route
    }
    NavHost(navController = navController, startDestination = startDest) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Scanner.route) {
            ScannerScreen(navController = navController)
        }
        composable(Screen.AppList.route) {
            AppListScreen(navController = navController)
        }
        composable(
            route = Screen.AppDetail.route,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) { backStackEntry ->
            AppDetailScreen(
                packageName = backStackEntry.arguments?.getString("packageName") ?: "",
                navController = navController
            )
        }
        composable(Screen.Accessibility.route) {
            AccessibilityScreen(navController = navController)
        }
        composable(Screen.Overlay.route) {
            OverlayScreen(navController = navController)
        }
        composable(Screen.Notifications.route) {
            NotificationScreen(navController = navController)
        }
        composable(Screen.Chrome.route) {
            ChromeScreen(navController = navController)
        }
        composable(Screen.Screenshot.route) {
            ScreenshotScreen(navController = navController)
        }
        composable(Screen.Report.route) {
            ReportScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
