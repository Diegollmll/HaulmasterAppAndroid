package app.forku.presentation.incident.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.forku.domain.model.incident.IncidentTypeEnum
import app.forku.presentation.incident.IncidentReportState
import app.forku.domain.model.incident.IncidentTypeFields
import app.forku.domain.model.incident.InjuryLocation
import app.forku.domain.model.incident.InjurySeverity
import app.forku.presentation.common.components.CustomOutlinedTextField
import app.forku.presentation.common.components.FormFieldDivider

@Composable
fun CollisionPeopleFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicPeopleFields(state, onValueChange, modifier)

    Column(modifier = modifier.fillMaxWidth()) {
        val collisionFields = (state.typeSpecificFields as? IncidentTypeFields.CollisionFields)
            ?: IncidentTypeFields.CollisionFields()

        InjurySeverityDropdown(
            selected = collisionFields.injurySeverity,
            onSelected = { severity ->
                onValueChange(state.copy(
                    typeSpecificFields = collisionFields.copy(
                        injurySeverity = severity,
                        // Clear injury locations if no injury is selected
                        injuryLocations = if (severity == InjurySeverity.NONE) emptyList() else collisionFields.injuryLocations
                    )
                ))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Only show injury locations if there are injuries
        if (collisionFields.injurySeverity != InjurySeverity.NONE) {
            FormFieldDivider()

            InjuryLocationsDropdown(
                selected = collisionFields.injuryLocations.mapNotNull { 
                    try { InjuryLocation.valueOf(it) } catch (e: Exception) { null }
                }.toSet(),
                onSelectionChanged = { locations ->
                    onValueChange(state.copy(
                        typeSpecificFields = collisionFields.copy(
                            injuryLocations = locations.map { it.name }
                        )
                    ))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun NearMissPeopleFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicPeopleFields(state, onValueChange, modifier)
}

@Composable
fun BasicPeopleFields(
    state: IncidentReportState,
    onValueChange: (IncidentReportState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        // Reporter field (read-only)
        if(true){
            CustomOutlinedTextField(
                value = state.userId ?: "Unknown",
                onValueChange = { },
                label = "Reported By",
                readOnly = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp)
            )
            Text(
                text = state.reporterName ?: "Unknown",
                modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 0.dp)
            )
            FormFieldDivider()
        }


        CustomOutlinedTextField(
            value = state.othersInvolved ?: "",
            onValueChange = { 
                onValueChange(state.copy(
                    othersInvolved = it
                ))
            },
            label = "Others Involved",
            minLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )
        if (state.type in listOf(IncidentTypeEnum.COLLISION)) {
            FormFieldDivider()
        }

    }
} 