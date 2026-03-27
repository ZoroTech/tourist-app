package com.example.tsa_shield.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsa_shield.R
import com.example.tsa_shield.utils.RiskLevel
import com.example.tsa_shield.utils.UserSafetyState
import com.example.tsa_shield.viewmodel.SafetyViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun DashboardScreen(
    viewModel: SafetyViewModel,
    onGoToProfile: () -> Unit,
    onGoToSettings: () -> Unit,
    onGoToAlerts: () -> Unit
) {
    val defaultPos = LatLng(28.6139, 77.2090)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPos, 15f)
    }

    val statusColor by animateColorAsState(
        targetValue = when (viewModel.safetyState) {
            UserSafetyState.SAFE -> Color(0xFF4CAF50)
            UserSafetyState.AT_RISK -> Color(0xFFFFA000)
            UserSafetyState.EMERGENCY -> Color(0xFFF44336)
        }, label = "StatusColor"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                if (viewModel.isMonitoringOn) viewModel.updateLocation(latLng)
            }
        ) {
            viewModel.userLocation?.let {
                Marker(state = MarkerState(position = it), title = "You")
            }

            Circle(
                center = LatLng(28.6139, 77.2090),
                radius = 500.0,
                fillColor = Color.Red.copy(alpha = 0.2f),
                strokeColor = Color.Red,
                strokeWidth = 2f
            )

            Circle(
                center = LatLng(28.6200, 77.2100),
                radius = 300.0,
                fillColor = Color.Green.copy(alpha = 0.2f),
                strokeColor = Color.Green,
                strokeWidth = 2f
            )
        }

        // Monitoring Toggle
        Card(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                Text("Monitoring", fontSize = 12.sp)
                Switch(
                    checked = viewModel.isMonitoringOn,
                    onCheckedChange = { viewModel.toggleMonitoring(it) },
                    modifier = Modifier.scale(0.7f)
                )
            }
        }

        // Safety State System UI
        Card(
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp).fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(containerColor = statusColor),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = viewModel.safetyState.name,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Risk Level: ${viewModel.riskLevel.name}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }

        // Demo Mode Button
        Button(
            onClick = { viewModel.simulateEmergency() },
            modifier = Modifier.align(Alignment.CenterStart).padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f))
        ) {
            Text("Simulate", fontSize = 10.sp)
        }

        // Action buttons
        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 100.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(onClick = onGoToAlerts, containerColor = MaterialTheme.colorScheme.surface) {
                Icon(Icons.Default.Notifications, "Incidents")
            }
            FloatingActionButton(onClick = onGoToSettings, containerColor = MaterialTheme.colorScheme.surface) {
                Icon(Icons.Default.Settings, "Settings")
            }
            FloatingActionButton(onClick = onGoToProfile, containerColor = MaterialTheme.colorScheme.surface) {
                Text("ID")
            }
        }

        // SOS / Cancel Row
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp)) {
            if (viewModel.isEmergencyTriggered) {
                Button(
                    onClick = { viewModel.cancelSOS() },
                    modifier = Modifier.height(60.dp).width(200.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("CANCEL EMERGENCY", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { viewModel.triggerSOS() },
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    elevation = ButtonDefaults.buttonElevation(12.dp)
                ) {
                    Text("SOS", fontWeight = FontWeight.Black, fontSize = 24.sp)
                }
            }
        }
    }
}
