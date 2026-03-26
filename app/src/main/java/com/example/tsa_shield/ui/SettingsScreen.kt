package com.example.tsa_shield.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.tsa_shield.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onResetProfile: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

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
            
            Spacer(modifier = Modifier.weight(1f))

            Divider()
            ListItem(
                modifier = Modifier.clickable { showDeleteDialog = true },
                headlineContent = { Text(stringResource(R.string.reset_data), color = Color.Red) },
                supportingContent = { Text(stringResource(R.string.reset_data_desc)) },
                trailingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.reset_confirm_title)) },
                text = { Text(stringResource(R.string.reset_confirm_msg)) },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        onResetProfile()
                    }) {
                        Text(stringResource(R.string.reset), color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
