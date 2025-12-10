package com.example.myapplication

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.Crossfade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.data.Repository
import com.example.myapplication.ui.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // initialize repository for local persistence
        Repository.init(applicationContext)
        
        try {
            // Create demo account if it doesn't exist
            if (Repository.getUserByEmail("teacher@demo.com") == null) {
                Repository.saveUser(
                    com.example.myapplication.data.User(
                        id = java.util.UUID.randomUUID().toString(),
                        email = "teacher@demo.com",
                        password = "demo123",
                        name = "Demo Teacher",
                        role = com.example.myapplication.data.UserRole.TEACHER
                    )
                )
                android.util.Log.d("MainActivity", "Demo user created")
            }
            
            // Initialize demo data for first-time users
            Repository.initializeDemoData(applicationContext)
            android.util.Log.d("MainActivity", "Demo data initialized")
            
            // Verify data was loaded
            val questionCount = Repository.loadQuestions().size
            val studentCount = Repository.loadStudents().size
            val scanCount = Repository.loadScans().size
            android.util.Log.d("MainActivity", "Loaded: $questionCount questions, $studentCount students, $scanCount scans")
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error initializing demo data", e)
        }
        
        enableEdgeToEdge()

        setContent {
            var darkTheme by rememberSaveable { mutableStateOf(false) }
            var userSession by rememberSaveable { mutableStateOf<com.example.myapplication.data.UserSession?>(null) }

            MyApplicationTheme(darkTheme = darkTheme) {
                // crossfade content when theme changes for a smooth transition
                Crossfade(targetState = darkTheme) { _ ->
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        if (userSession == null) {
                            // Show authentication screens
                            AuthNavigation(
                                onLoginSuccess = { session ->
                                    userSession = session
                                }
                            )
                        } else {
                            // Show main app
                            val navController = rememberNavController()

                            // Permissions: request CAMERA on startup if not granted
                            val cameraPermissionGranted = remember { mutableStateOf(false) }
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.RequestPermission(),
                                onResult = { granted -> cameraPermissionGranted.value = granted }
                            )

                            LaunchedEffect(Unit) {
                                launcher.launch(Manifest.permission.CAMERA)
                            }

                            AppNavHost(
                                navController = navController,
                                cameraPermissionGranted = cameraPermissionGranted.value,
                                darkTheme = darkTheme,
                                onToggleTheme = { darkTheme = !darkTheme },
                                userSession = userSession!!,
                                onLogout = { userSession = null }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthNavigation(onLoginSuccess: (com.example.myapplication.data.UserSession) -> Unit) {
    var showRegister by remember { mutableStateOf(false) }
    
    if (showRegister) {
        com.example.myapplication.ui.screens.RegisterScreen(
            onRegistrationSuccess = {
                showRegister = false
            },
            onNavigateBack = {
                showRegister = false
            }
        )
    } else {
        com.example.myapplication.ui.screens.LoginScreen(
            onLoginSuccess = onLoginSuccess,
            onNavigateToRegister = {
                showRegister = true
            }
        )
    }
}
