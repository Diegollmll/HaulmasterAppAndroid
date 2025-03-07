package app.forku.domain.repository.checklist

interface ChecklistStatusNotifier {
    suspend fun notifyCheckStatusChanged(vehicleId: String, checkStatus: String)
}   