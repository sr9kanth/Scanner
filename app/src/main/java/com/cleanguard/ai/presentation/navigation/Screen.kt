package com.cleanguard.ai.presentation.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Scanner : Screen("scanner")
    object AppList : Screen("app_list")
    object AppDetail : Screen("app_detail/{packageName}") {
        fun createRoute(packageName: String) = "app_detail/$packageName"
    }
    object Accessibility : Screen("accessibility")
    object Overlay : Screen("overlay")
    object Notifications : Screen("notifications")
    object Chrome : Screen("chrome")
    object Screenshot : Screen("screenshot")
    object Report : Screen("report")
    object Settings : Screen("settings")
}
