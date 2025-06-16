package app.forku.domain.model.session

import app.forku.domain.model.checklist.ChecklistAnswer
import app.forku.domain.model.user.User
import app.forku.domain.model.vehicle.Vehicle

data class AdminDashboardData(
    val activeSessions: List<VehicleSession>,
    val vehicles: Map<String, Vehicle>, // vehicleId -> Vehicle
    val operators: Map<String, User>, // userId -> User  
    val checklistAnswers: Map<String, ChecklistAnswer> // checklistAnswerId -> ChecklistAnswer
) 