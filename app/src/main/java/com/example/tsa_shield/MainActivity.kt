package com.example.tsa_shield

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tsa_shield.ui.AlertHistoryScreen
import com.example.tsa_shield.ui.AuthScreen
import com.example.tsa_shield.ui.DashboardScreen
import com.example.tsa_shield.ui.ProfileScreen
import com.example.tsa_shield.ui.SettingsScreen
import com.example.tsa_shield.ui.SplashScreen
import com.example.tsa_shield.ui.theme.TSA_SHIELDTheme
import com.example.tsa_shield.utils.LocationManager
import com.example.tsa_shield.viewmodel.SafetyViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: SafetyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TSA_SHIELDTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationPermissionWrapper {
                        AppNavigation(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun LocationPermissionWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val viewModel: SafetyViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            startTracking(context, viewModel)
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasFineLocation) {
            startTracking(context, viewModel)
        } else {
            launcher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }
    
    content()
}

private fun startTracking(context: android.content.Context, viewModel: SafetyViewModel) {
    val locationManager = LocationManager(context)
    locationManager.startLocationUpdates { latLng ->
        viewModel.updateLocation(latLng)
    }
}

@Composable
fun AppNavigation(viewModel: SafetyViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onTimeout = {
                val destination = if (viewModel.userProfile != null) "dashboard" else "auth"
                navController.navigate(destination) {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("auth") {
            AuthScreen(viewModel, onAuthSuccess = {
                navController.navigate("dashboard") {
                    popUpTo("auth") { inclusive = true }
                }
            })
        }
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onGoToProfile = { navController.navigate("profile") },
                onGoToSettings = { navController.navigate("settings") },
                onGoToAlerts = { navController.navigate("alerts") }
            )
        }
        composable("profile") {
            ProfileScreen(viewModel, onBack = {
                navController.popBackStack()
            })
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onResetProfile = {
                    viewModel.resetProfileData()
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("alerts") {
            AlertHistoryScreen(viewModel, onBack = {
                navController.popBackStack()
            })
        }
    }
}
