package com.mvukosav.scoreagentsvas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mvukosav.scoreagentsvas.match.data.repository.NotificationsAgent
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository
import com.mvukosav.scoreagentsvas.match.data.repository.SharedMatchesRepository.notificationService
import com.mvukosav.scoreagentsvas.match.presentation.home.HomeScreenViewModel
import com.mvukosav.scoreagentsvas.match.presentation.matchdetails.MatchDetailsScreen
import com.mvukosav.scoreagentsvas.ui.theme.ScoreAgentsVASTheme
import com.mvukosav.scoreagentsvas.match.ui.HomeScreen
import com.mvukosav.scoreagentsvas.user.ui.LoginScreen
import com.mvukosav.scoreagentsvas.user.ui.splash.SplashScreen
import com.mvukosav.scoreagentsvas.utils.AgentsNotificationServiceImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScoreAgentsVASTheme {
                ScoreAgentsApp()
            }
        }
    }
}

@Composable
fun ScoreAgentsApp(
    service: AgentsNotificationServiceImpl? = null,
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val homeViewModel: HomeScreenViewModel = hiltViewModel()
    val navControler = rememberNavController()

    LaunchedEffect(key1 = true) {
        viewModel.events.collectLatest {
            when (it) {
                MainUIEvent.NavigateToHome -> navControler.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }

                MainUIEvent.NavigateToLogin -> navControler.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }

                null -> {}
            }
        }
    }

    NavHost(navController = navControler, startDestination = "splash") {
        composable("splash") {
            SplashScreen()
        }

        composable("login") {
            LoginScreen()
        }
        composable("match_details/?matchId={matchId}", arguments = listOf(navArgument("matchId") {
            type = NavType.IntType
            defaultValue = -1
        })) {
            MatchDetailsScreen(navController = navControler)
        }

        composable("home") {
            HomeScreen(viewModel = homeViewModel, navController = navControler)
        }
    }
}