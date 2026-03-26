package com.example.tsa_shield.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tsa_shield.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
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
        ) {
            Text(text = stringResource(R.string.app_preferences), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            ListItem(
                headlineContent = { Text(stringResource(R.string.language)) },
                supportingContent = { Text(stringResource(R.string.language_desc)) },
                trailingContent = { Text("English / Hindi") }
            )
            Divider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.notifications)) },
                supportingContent = { Text(stringResource(R.string.notifications_desc)) },
                trailingContent = { Switch(checked = true, onCheckedChange = {}) }
            )
            Divider()
            ListItem(
                headlineContent = { Text(stringResource(R.string.location_tracking)) },
                supportingContent = { Text(stringResource(R.string.location_tracking_desc)) },
                trailingContent = { Switch(checked = true, onCheckedChange = {}) }
            )
        }
    }
}
