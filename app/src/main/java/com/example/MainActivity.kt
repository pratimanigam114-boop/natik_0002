package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.AdMobInitializer
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.screens.TravelScreen
import com.example.ui.theme.AILifeAssistantTheme

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Tasks : Screen("tasks", "Tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircleOutline)
    object Calendar : Screen("calendar", "Calendar", Icons.Filled.CalendarToday, Icons.Outlined.CalendarToday)
    object Assistant : Screen("assistant", "Assistant", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    object Finance : Screen("finance", "Finance", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdMobInitializer.initialize(this)
        enableEdgeToEdge()
        setContent {
            AILifeAssistantTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var currentRoute by remember { mutableStateOf("home") }
    var previousRoute by remember { mutableStateOf("home") }

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Handled */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Back handling for secondary screens
    BackHandler(enabled = currentRoute != "home") {
        currentRoute = if (currentRoute in listOf("travel", "subscription", "analytics", "settings", "bills")) {
            previousRoute
        } else {
            "home"
        }
    }

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Tasks,
        Screen.Calendar,
        Screen.Assistant,
        Screen.Finance
    )

    val isTopLevelScreen = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("main_bottom_nav_bar")
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                previousRoute = currentRoute
                                currentRoute = screen.route
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentRoute,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { route ->
                when (route) {
                    "home" -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { target ->
                            previousRoute = currentRoute
                            currentRoute = target
                        },
                        onOpenAiChat = {
                            previousRoute = currentRoute
                            currentRoute = "assistant"
                        }
                    )
                    "tasks" -> TasksScreen(viewModel = viewModel)
                    "calendar" -> CalendarScreen(viewModel = viewModel)
                    "assistant" -> AssistantScreen(
                        viewModel = viewModel,
                        onNavigate = { target ->
                            previousRoute = currentRoute
                            currentRoute = target
                        }
                    )
                    "finance" -> FinanceScreen(viewModel = viewModel)
                    "bills" -> FinanceScreen(viewModel = viewModel) // direct tab inside Finance
                    "travel" -> TravelScreen(
                        viewModel = viewModel,
                        onOpenAiPlanner = {
                            previousRoute = currentRoute
                            currentRoute = "assistant"
                        }
                    )
                    "subscription" -> SubscriptionScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentRoute = previousRoute }
                    )
                    "analytics" -> AnalyticsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentRoute = previousRoute }
                    )
                    "settings" -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentRoute = previousRoute },
                        onNavigate = { target ->
                            previousRoute = currentRoute
                            currentRoute = target
                        }
                    )
                    else -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { target -> currentRoute = target },
                        onOpenAiChat = { currentRoute = "assistant" }
                    )
                }
            }
        }
    }
}
