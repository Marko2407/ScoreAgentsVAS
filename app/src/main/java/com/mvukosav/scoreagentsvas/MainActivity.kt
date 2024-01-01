package com.mvukosav.scoreagentsvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mvukosav.scoreagentsvas.ui.theme.ScoreAgentsVASTheme
import com.mvukosav.scoreagentsvas.match.ui.HomeScreen
import com.mvukosav.scoreagentsvas.user.ui.LoginScreen
import com.mvukosav.scoreagentsvas.user.ui.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
fun ScoreAgentsApp(viewModel: MainActivityViewModel = hiltViewModel()) {
    val navControler = rememberNavController()

    LaunchedEffect(key1 = true) {
        viewModel.events.collectLatest {
            when (it) {
                MainUIEvent.NavigateToHome -> navControler.navigate("home") {
                    popUpTo(navControler.graph.id) {
                        inclusive = true
                    }
                }

                MainUIEvent.NavigateToLogin -> navControler.navigate("login") {
                    popUpTo(navControler.graph.id) {
                        inclusive = true
                    }
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

        composable("home") {
            HomeScreen()
        }
    }
}