package com.zengaishua.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zengaishua.app.ui.screen.MineScreen
import com.zengaishua.app.ui.screen.PracticeScreen
import com.zengaishua.app.ui.screen.QuestionScreen
import com.zengaishua.app.ui.theme.ZasTheme
import com.zengaishua.app.ui.viewmodel.MineViewModel
import com.zengaishua.app.ui.viewmodel.PracticeViewModel

sealed class Screen(val route: String, val title: String) {
    object Practice : Screen("practice", "刷题")
    object Mine : Screen("mine", "我的")
    object Question : Screen("question/{bankId}/{mode}", "答题") {
        fun createRoute(bankId: String, mode: String) = "question/$bankId/$mode"
    }
}

class MainActivity : ComponentActivity() {
    private val practiceViewModel: PracticeViewModel by viewModels()
    private val mineViewModel: MineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZasTheme {
                MainScreen(
                    practiceViewModel = practiceViewModel,
                    mineViewModel = mineViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    practiceViewModel: PracticeViewModel,
    mineViewModel: MineViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarScreens = listOf(
        Screen.Practice,
        Screen.Mine
    )

    val showBottomBar = currentDestination?.route in bottomBarScreens.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarScreens.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    when (screen) {
                                        Screen.Practice -> Icons.Default.School
                                        Screen.Mine -> Icons.Default.Person
                                        else -> Icons.Default.School
                                    },
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Practice.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Practice.route) {
                PracticeScreen(
                    viewModel = practiceViewModel,
                    onBankClick = { bank ->
                        practiceViewModel.selectBank(bank)
                        navController.navigate(
                            Screen.Question.createRoute(bank.id, "normal")
                        )
                    },
                    onFavoritesClick = { bankId ->
                        practiceViewModel.loadFavorites(bankId)
                        navController.navigate(
                            Screen.Question.createRoute(bankId, "favorites")
                        )
                    },
                    onWrongQuestionsClick = { bankId ->
                        practiceViewModel.loadWrongQuestions(bankId)
                        navController.navigate(
                            Screen.Question.createRoute(bankId, "wrong")
                        )
                    }
                )
            }

            composable(Screen.Mine.route) {
                MineScreen(viewModel = mineViewModel)
            }

            composable(Screen.Question.route) {
                QuestionScreen(
                    viewModel = practiceViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
