package com.paintpro.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.paintpro.app.ui.attendance.AttendanceScreen
import com.paintpro.app.ui.auth.SignInScreen
import com.paintpro.app.ui.auth.SignUpScreen
import com.paintpro.app.ui.common.rememberAppContainer
import com.paintpro.app.ui.dashboard.DashboardScreen
import com.paintpro.app.ui.labours.LabourFormScreen
import com.paintpro.app.ui.labours.LabourListScreen
import com.paintpro.app.ui.settings.SettingsScreen
import com.paintpro.app.ui.sites.SiteFormScreen
import com.paintpro.app.ui.sites.SiteListScreen
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

/**
 * Root composable: waits for the persisted Supabase session (if any) to finish loading, then
 * hosts the whole app behind a single [NavHost]. A sign-out from anywhere collapses the back
 * stack straight back to the sign-in screen.
 */
@Composable
fun PaintProApp() {
    val container = rememberAppContainer()
    val authRepository = container.authRepository
    val scope = rememberCoroutineScope()

    var initialized by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        authRepository.awaitInitialization()
        initialized = true
    }

    if (!initialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val sessionStatus by authRepository.sessionStatus.collectAsStateWithLifecycle()

    LaunchedEffect(sessionStatus) {
        if (sessionStatus is SessionStatus.NotAuthenticated) {
            navController.navigate(PaintProDestinations.SIGN_IN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val startDestination = if (authRepository.currentUserId() != null) {
        PaintProDestinations.DASHBOARD
    } else {
        PaintProDestinations.SIGN_IN
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(PaintProDestinations.SIGN_IN) {
            SignInScreen(
                onSignedIn = {
                    navController.navigate(PaintProDestinations.DASHBOARD) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSignUp = { navController.navigate(PaintProDestinations.SIGN_UP) },
            )
        }
        composable(PaintProDestinations.SIGN_UP) {
            SignUpScreen(
                onSignedUp = {
                    navController.navigate(PaintProDestinations.DASHBOARD) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToSignIn = { navController.popBackStack() },
            )
        }
        composable(PaintProDestinations.DASHBOARD) {
            DashboardScreen(
                onNavigateToSites = { navController.navigate(PaintProDestinations.SITES) },
                onNavigateToLabours = { navController.navigate(PaintProDestinations.LABOURS) },
                onNavigateToAttendance = { navController.navigate(PaintProDestinations.ATTENDANCE) },
                onNavigateToSettings = { navController.navigate(PaintProDestinations.SETTINGS) },
                onSignOut = { scope.launch { authRepository.signOut() } },
            )
        }
        composable(PaintProDestinations.SITES) {
            SiteListScreen(
                onAddSite = { navController.navigate(PaintProDestinations.siteForm()) },
                onEditSite = { siteId -> navController.navigate(PaintProDestinations.siteForm(siteId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = PaintProDestinations.SITE_FORM,
            arguments = listOf(navArgument(PaintProDestinations.SITE_FORM_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }),
        ) { backStackEntry ->
            val siteId = backStackEntry.arguments?.getString(PaintProDestinations.SITE_FORM_ARG)?.ifBlank { null }
            SiteFormScreen(siteId = siteId, onDone = { navController.popBackStack() })
        }
        composable(PaintProDestinations.LABOURS) {
            LabourListScreen(
                onAddLabour = { navController.navigate(PaintProDestinations.labourForm()) },
                onEditLabour = { labourId -> navController.navigate(PaintProDestinations.labourForm(labourId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = PaintProDestinations.LABOUR_FORM,
            arguments = listOf(navArgument(PaintProDestinations.LABOUR_FORM_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }),
        ) { backStackEntry ->
            val labourId = backStackEntry.arguments?.getString(PaintProDestinations.LABOUR_FORM_ARG)?.ifBlank { null }
            LabourFormScreen(labourId = labourId, onDone = { navController.popBackStack() })
        }
        composable(PaintProDestinations.ATTENDANCE) {
            AttendanceScreen(onBack = { navController.popBackStack() })
        }
        composable(PaintProDestinations.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
