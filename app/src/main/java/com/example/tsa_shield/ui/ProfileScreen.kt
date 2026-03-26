package com.example.tsa_shield.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsa_shield.R
import com.example.tsa_shield.viewmodel.SafetyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: SafetyViewModel, onBack: () -> Unit) {
    val profile = viewModel.userProfile
    val isTampered = viewModel.isDataTampered

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.digital_id_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (profile != null) {
                // Integrity Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTampered) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isTampered) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isTampered) Color.Red else Color(0xFF43A047)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTampered) stringResource(R.string.integrity_fail) else stringResource(R.string.integrity_ok),
                            color = if (isTampered) Color.Red else Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.personal_info), style = MaterialTheme.typography.titleLarge)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        ProfileItem(label = stringResource(R.string.full_name), value = profile.name)
                        ProfileItem(label = stringResource(R.string.email), value = profile.email)
                        ProfileItem(label = stringResource(R.string.emergency_contact), value = profile.emergencyContact)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.blockchain_verify), style = MaterialTheme.typography.titleMedium, color = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.blockchain_desc),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color.White,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = profile.profileHash,
                                modifier = Modifier.padding(8.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.no_profile))
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}
