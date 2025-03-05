
package app.forku.presentation.vehicle.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.domain.model.vehicle.VehicleStatus
import app.forku.domain.model.vehicle.toColor


@Composable
fun VehicleStatusIndicator(status: VehicleStatus) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = status.toColor(),
                    shape = CircleShape
                )
        )
        Text(
            text = status.name,
            style = MaterialTheme.typography.bodySmall,
            color = status.toColor()
        )
    }
} 