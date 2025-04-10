package app.forku.presentation.checklist.category

/**
 * Represents a questionary checklist item category in the application.
 * 
 * @property id Unique identifier for the category
 * @property name Name of the category
 * @property description Optional description of the category
 * @property priority Priority level of the category (higher number means higher priority)
 * @property createdAt Timestamp when the category was created
 * @property updatedAt Timestamp when the category was last updated
 */
data class QuestionaryChecklistItemCategory(
    val id: String,
    val name: String,
    val description: String = "",
    val priority: Int = 5,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) 