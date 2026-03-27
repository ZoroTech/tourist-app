package com.example.tsa_shield.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsa_shield.data.IncidentEntity
import com.example.tsa_shield.viewmodel.SafetyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentHistoryScreen(viewModel: SafetyViewModel, onBack: () -> Unit) {
    val incidents = viewModel.incidentHistory

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incident History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (incidents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No incidents recorded")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(incidents) { incident ->
                    IncidentItem(incident)
                }
            }
        }
    }
}

@Composable
fun IncidentItem(incident: IncidentEntity) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    val dateString = sdf.format(Date(incident.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (incident.riskLevel) {
                "HIGH" -> Color(0xFFFFEBEE)
                "MEDIUM" -> Color(0xFFFFF3E0)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = incident.type, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(text = incident.riskLevel, fontWeight = FontWeight.Bold, color = if (incident.riskLevel == "HIGH") Color.Red else Color.Unspecified)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = dateString, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Location: ${incident.latitude}, ${incident.longitude}", fontSize = 12.sp)
        }
    }
}
