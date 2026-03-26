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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsa_shield.R
import com.example.tsa_shield.data.SafetyAlert
import com.example.tsa_shield.viewmodel.SafetyViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertHistoryScreen(viewModel: SafetyViewModel, onBack: () -> Unit) {
    val alerts = viewModel.alertHistory

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.safety_log)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_alerts))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(alerts) { alert ->
                    AlertItem(alert)
                }
            }
        }
    }
}

@Composable
fun AlertItem(alert: SafetyAlert) {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = sdf.format(Date(alert.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.alertType.contains("SOS")) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alert.alertType,
                    fontWeight = FontWeight.Bold,
                    color = if (alert.alertType.contains("SOS")) Color.Red else MaterialTheme.colorScheme.primary
                )
                Text(text = dateString, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = alert.description, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Location: ${String.format("%.4f", alert.latitude)}, ${String.format("%.4f", alert.longitude)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
