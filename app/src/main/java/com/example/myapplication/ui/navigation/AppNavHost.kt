package com.example.myapplication.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myapplication.ui.screens.DashboardScreen
import com.example.myapplication.ui.screens.QuestionBankScreen
import com.example.myapplication.ui.screens.ResultsScreen
import com.example.myapplication.ui.screens.ScanScreen
import com.example.myapplication.ui.screens.ScanHistoryScreen
import com.example.myapplication.ui.screens.GradingWorkflowScreen
import com.example.myapplication.ui.screens.StudentManagementScreen
import com.example.myapplication.ui.screens.ClassManagementScreen
import com.example.myapplication.data.Repository

private data class BottomItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    cameraPermissionGranted: Boolean,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    userSession: com.example.myapplication.data.UserSession,
    onLogout: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }
    
    val items = remember {
        listOf(
            BottomItem("dashboard", "Home", Icons.Default.Home),
            BottomItem("scan", "Scan", Icons.Default.PhotoCamera),
            BottomItem("results", "Results", Icons.Default.Assessment)
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("AI Exam Grader") },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        androidx.compose.animation.Crossfade(targetState = darkTheme) { isDark ->
                            if (isDark) {
                                Icon(Icons.Default.Brightness7, contentDescription = "Switch to light")
                            } else {
                                Icon(Icons.Default.Brightness4, contentDescription = "Switch to dark")
                            }
                        }
                    }
                    
                    // User Menu
                    IconButton(onClick = { showUserMenu = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Account")
                    }
                    
                    DropdownMenu(
                        expanded = showUserMenu,
                        onDismissRequest = { showUserMenu = false }
                    ) {
                        // User info
                        DropdownMenuItem(
                            text = {
                                androidx.compose.foundation.layout.Column {
                                    Text(
                                        text = userSession.name,
                                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = userSession.email,
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { }
                        )
                        androidx.compose.material3.HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Row {
                                    Icon(Icons.Default.Logout, contentDescription = "Logout")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Logout")
                                }
                            },
                            onClick = {
                                showUserMenu = false
                                onLogout()
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { 
                            navController.navigate(item.route) { 
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            } 
                        },
                        icon = { 
                            Icon(
                                item.icon, 
                                contentDescription = item.label,
                                modifier = Modifier.size(if (currentRoute == item.route) 26.dp else 24.dp)
                            ) 
                        },
                        label = { 
                            Text(
                                item.label,
                                fontWeight = if (currentRoute == item.route) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = "dashboard", 
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(navController) }
            composable("scan") { ScanScreen(navController, cameraPermissionGranted) }
            composable("results") { ResultsScreen(navController) }
            composable("questionbank") { QuestionBankScreen(navController) }
            composable("scanhistory") { ScanHistoryScreen(navController) }
            composable("grading") { GradingWorkflowScreen(navController) }
            composable("students") {
                val context = LocalContext.current
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    Repository.init(context)
                }
                StudentManagementScreen(
                    repository = Repository,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("classes") {
                val context = LocalContext.current
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    Repository.init(context)
                }
                ClassManagementScreen(
                    repository = Repository,
                    teacherId = userSession.userId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
