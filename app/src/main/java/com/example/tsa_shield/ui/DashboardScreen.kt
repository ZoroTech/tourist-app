package com.example.tsa_shield.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsa_shield.R
import com.example.tsa_shield.utils.SafetyState
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
    val defaultPos = LatLng(28.6139, 77.2090) // Delhi
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPos, 15f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                viewModel.updateLocation(latLng)
            }
        ) {
            viewModel.userLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Current Location"
                )
            }

            Circle(
                center = LatLng(28.6139, 77.2090),
                radius = 500.0,
                fillColor = Color.Red.copy(alpha = 0.3f),
                strokeColor = Color.Red,
                strokeWidth = 2f
            )

            Circle(
                center = LatLng(28.6200, 77.2100),
                radius = 300.0,
                fillColor = Color.Green.copy(alpha = 0.3f),
                strokeColor = Color.Green,
                strokeWidth = 2f
            )
        }

        // Top Status Indicator
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = when (viewModel.safetyStatus) {
                    SafetyState.SAFE -> Color(0xFF4CAF50)
                    SafetyState.STATIONARY_TOO_LONG -> Color(0xFFFFA000)
                    SafetyState.UNSAFE_ZONE -> Color(0xFFF44336)
                }
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (viewModel.safetyStatus) {
                        SafetyState.SAFE -> stringResource(R.string.status_safe)
                        SafetyState.STATIONARY_TOO_LONG -> stringResource(R.string.stationary_risk)
                        SafetyState.UNSAFE_ZONE -> stringResource(R.string.status_risk)
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = when (viewModel.safetyStatus) {
                        SafetyState.SAFE -> stringResource(R.string.safe_desc)
                        SafetyState.STATIONARY_TOO_LONG -> stringResource(R.string.stationary_desc)
                        SafetyState.UNSAFE_ZONE -> stringResource(R.string.risk_desc)
                    },
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Action Buttons Column (on the right)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onGoToSettings) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 4.dp) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.padding(8.dp))
                }
            }
            IconButton(onClick = onGoToAlerts) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 4.dp) {
                    Icon(Icons.Default.List, contentDescription = "Alert History", modifier = Modifier.padding(8.dp))
                }
            }
            IconButton(onClick = onGoToProfile) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, tonalElevation = 4.dp) {
                    Text("ID", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                }
            }
        }

        // "I'm Safe" Reset Button for stationary risk
        AnimatedVisibility(
            visible = viewModel.isStayingTooLong,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Button(
                onClick = { viewModel.resetSafetyStatus() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA000)),
                modifier = Modifier.padding(bottom = 200.dp)
            ) {
                Text(stringResource(R.string.im_safe), fontWeight = FontWeight.Bold)
            }
        }

        // SOS Button
        Button(
            onClick = { viewModel.triggerSOS() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
                .size(110.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            elevation = ButtonDefaults.buttonElevation(12.dp)
        ) {
            Text(
                text = stringResource(R.string.sos_button),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }

        if (viewModel.isEmergencyTriggered) {
            AlertDialog(
                onDismissRequest = { viewModel.isEmergencyTriggered = false },
                confirmButton = {
                    TextButton(onClick = { viewModel.isEmergencyTriggered = false }) {
                        Text("OK")
                    }
                },
                title = { Text(stringResource(R.string.emergency_alert_title)) },
                text = { Text(stringResource(R.string.emergency_alert_desc)) }
            )
        }
    }
}
