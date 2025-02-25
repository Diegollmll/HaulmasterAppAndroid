import app.forku.domain.model.checklist.Answer

data class PreShiftCheck(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val datetime: String,
    val status: String,
    val items: List<CheckedItem>
)

data class CheckedItem(
    val id: String,
    val expectedAnswer: Answer,
    val userAnswer: Answer
) 