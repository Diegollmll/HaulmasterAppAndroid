package app.forku.presentation.timezones

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.forku.core.network.NetworkConnectivityManager
import app.forku.presentation.common.components.BaseScreen
import java.time.ZoneId

@Composable
fun TimeZonesScreen(
    navController: NavController,
    networkManager: NetworkConnectivityManager,
    viewModel: TimeZonesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    BaseScreen(
        navController = navController,
        showBottomBar = false,
        showTopBar = true,
        showBackButton = true,
        topBarTitle = "Time Zones",
        networkManager = networkManager
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search time zones...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time zones list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.filteredTimeZones) { timeZone ->
                    TimeZoneItem(
                        timeZone = timeZone,
                        isSelected = timeZone.id == state.selectedTimeZone?.id,
                        onSelect = { viewModel.selectTimeZone(timeZone) }
                    )
                }
            }

            // Selected time zone details
            state.selectedTimeZone?.let { timeZone ->
                Spacer(modifier = Modifier.height(16.dp))
                TimeZoneDetails(timeZone = timeZone)
            }
        }
    }
}

@Composable
private fun TimeZoneItem(
    timeZone: TimeZoneInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeZone.id,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = timeZone.offset,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TimeZoneDetails(timeZone: TimeZoneInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Time Zone Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow("ID", timeZone.id)
            DetailRow("Offset", timeZone.offset)
            DetailRow("Region", timeZone.region)
            DetailRow("Current Time", timeZone.currentTime)
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

data class TimeZoneInfo(
    val id: String,
    val offset: String,
    val region: String,
    val currentTime: String
) {
    companion object {
        fun fromZoneId(zoneId: ZoneId): TimeZoneInfo {
            val now = java.time.ZonedDateTime.now(zoneId)
            val offset = now.offset.toString()
            val region = zoneId.id.split("/").let { parts ->
                if (parts.size > 1) parts[0] else "Other"
            }
            val currentTime = now.format(java.time.format.DateTimeFormatter.ISO_TIME)
            
            return TimeZoneInfo(
                id = zoneId.id,
                offset = offset,
                region = region,
                currentTime = currentTime
            )
        }
    }
} 