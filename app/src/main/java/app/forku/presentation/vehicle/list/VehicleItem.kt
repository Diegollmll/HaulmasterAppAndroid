package app.forku.presentation.vehicle.list

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import app.forku.core.Constants.BASE_URL
import app.forku.domain.model.checklist.CheckStatus
import app.forku.domain.model.checklist.ChecklistAnswer
import app.forku.domain.model.checklist.PreShiftCheck
import app.forku.domain.model.checklist.getPreShiftStatusColor
import app.forku.domain.model.checklist.getPreShiftStatusText
import app.forku.domain.model.session.VehicleSessionInfo
import coil.compose.AsyncImage
import app.forku.presentation.common.components.UserDateTimer
import app.forku.domain.model.vehicle.Vehicle
import app.forku.domain.model.user.UserRole
import app.forku.domain.model.vehicle.toColor
import app.forku.domain.model.vehicle.toDisplayString
import app.forku.presentation.common.utils.parseDateTime
import app.forku.presentation.vehicle.components.VehicleImage
import coil.ImageLoader
import app.forku.presentation.common.utils.getUserAvatarData
import app.forku.presentation.common.components.UserAvatar

data class TextConfig(
    val fontSize: Int,
    val lineHeight: Int = (fontSize * 1.2).toInt(),
    val fontWeight: FontWeight = FontWeight.Normal,
    val color: Color = Color.Unspecified
)

@Composable
private fun StatusChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    textConfig: TextConfig = TextConfig(fontSize = 10),
    horizontalPadding: Int = 4,
    verticalPadding: Int = 2,
    shape: Int = 4
) {
    Surface(
        color = backgroundColor.copy(alpha = 0.1f),
        shape = RoundedCornerShape(shape.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            style = TextStyle(
                fontSize = textConfig.fontSize.sp,
                lineHeight = textConfig.lineHeight.sp,
                fontWeight = textConfig.fontWeight
            ),
            modifier = Modifier.padding(horizontal = horizontalPadding.dp, vertical = verticalPadding.dp),
            maxLines = 1
        )
    }
}

data class StatusChipConfig(
    val horizontalPadding: Int = 4,
    val verticalPadding: Int = 2,
    val cornerRadius: Int = 4
)

data class VehicleItemTextConfigs(
    val codename: TextConfig = TextConfig(
        fontSize = 14,
        lineHeight = 16,
        fontWeight = FontWeight.Bold
    ),
    val status: TextConfig = TextConfig(
        fontSize = 10,
        lineHeight = 12
    ),
    val vehicleType: TextConfig = TextConfig(
        fontSize = 11,
        lineHeight = 13,
        color = Color.Gray
    ),
    val timer: TextConfig = TextConfig(
        fontSize = 13,
        lineHeight = 14
    ),
    val operatorLabel: TextConfig = TextConfig(
        fontSize = 10,
        lineHeight = 12,
        color = Color.Gray
    ),
    val operatorName: TextConfig = TextConfig(
        fontSize = 11,
        lineHeight = 13,
        fontWeight = FontWeight.Medium
    ),
    val preshiftCheck: TextConfig = TextConfig(
        fontSize = 10,
        lineHeight = 12,
        color = Color.Gray
    )
)

@Composable
fun VehicleItem(
    vehicle: Vehicle,
    userRole: UserRole,
    sessionInfo: VehicleSessionInfo? = null,
    showStatus: Boolean = true,
    statusChipConfig: StatusChipConfig = StatusChipConfig(),
    textConfigs: VehicleItemTextConfigs = VehicleItemTextConfigs(),
    lastPreShiftCheck: PreShiftCheck? = null,
    imageLoader: ImageLoader,
    checklistAnswer: ChecklistAnswer? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .widthIn(max = 800.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
        ) {
            VehicleImage(
                vehicleId = vehicle.id,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                imageLoader = imageLoader,
                contentScale = ContentScale.Inside
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Min),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Text(
                        text = vehicle.codename.replaceFirstChar { it.uppercase() },
                        style = TextStyle(
                            fontSize = textConfigs.codename.fontSize.sp,
                            lineHeight = textConfigs.codename.lineHeight.sp,
                            fontWeight = textConfigs.codename.fontWeight
                        ),
                        modifier = Modifier.padding(0.dp)
                    )

                    Spacer(Modifier.width(4.dp))

                    if (showStatus) {
                        StatusChip(
                            text = vehicle.status.toDisplayString(),
                            backgroundColor = vehicle.status.toColor(),
                            textColor = vehicle.status.toColor(),
                            textConfig = textConfigs.status,
                            horizontalPadding = statusChipConfig.horizontalPadding,
                            verticalPadding = statusChipConfig.verticalPadding,
                            shape = statusChipConfig.cornerRadius
                        )
                    }

                    Spacer(Modifier.weight(1f))
                }

                Text(
                    text = vehicle.type.Name,
                    style = TextStyle(
                        fontSize = textConfigs.vehicleType.fontSize.sp,
                        lineHeight = textConfigs.vehicleType.lineHeight.sp,
                        color = textConfigs.vehicleType.color
                    ),
                    modifier = Modifier.padding(0.dp)
                )

                // After displaying vehicle type/name, add checklist status if available
                if (checklistAnswer != null) {
                    Text(
                        text = "Checklist: ${ getPreShiftStatusText(checklistAnswer.status)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = getPreShiftStatusColor(getPreShiftStatusText(checklistAnswer.status)),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                if (sessionInfo?.sessionStartTime != null) {
                    val startInstant = remember(sessionInfo.sessionStartTime) {
                        try {
                            parseDateTime(sessionInfo.sessionStartTime).toInstant()
                        } catch (e: Exception) {
                            android.util.Log.e("VehicleItem", "Error parsing date: ${sessionInfo.sessionStartTime}", e)
                            null
                        }
                    }
                    UserDateTimer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                        sessionStartInstant = startInstant,
                        fontSize = textConfigs.timer.fontSize
                    )
                }
            }
            
            // Only show operator info if there's an active session
            if (sessionInfo?.sessionStartTime != null && sessionInfo.operatorName != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .width(90.dp)
                        .height(IntrinsicSize.Min),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    val avatarData = getUserAvatarData(
                        sessionInfo.operator?.firstName,
                        sessionInfo.operator?.lastName,
                        sessionInfo.operatorImage
                    )
                    Log.d("VehicleItem", "operatorName value: ${sessionInfo.operatorName}")
                    Log.d("VehicleItem", "operatorImage value: ${sessionInfo.operatorImage}")
                    UserAvatar(avatarData = avatarData, size = 32.dp, fontSize = 14.sp)
                    Text(
                        text = "Operator",
                        style = TextStyle(
                            fontSize = textConfigs.operatorLabel.fontSize.sp,
                            lineHeight = textConfigs.operatorLabel.lineHeight.sp,
                            color = textConfigs.operatorLabel.color
                        ),
                        modifier = Modifier.padding(0.dp)
                    )
                    Text(
                        text = sessionInfo.operatorName.ifBlank { "Sin nombre" },
                        style = TextStyle(
                            fontSize = textConfigs.operatorName.fontSize.sp,
                            lineHeight = textConfigs.operatorName.lineHeight.sp,
                            fontWeight = textConfigs.operatorName.fontWeight
                        ),
                        maxLines = 1,
                        modifier = Modifier.padding(0.dp)
                    )
                }
            }
        }
    }
} 