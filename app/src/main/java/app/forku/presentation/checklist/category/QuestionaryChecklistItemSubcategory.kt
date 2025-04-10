package app.forku.presentation.checklist.category

/**
 * Represents a questionary checklist item subcategory in the application.
 * 
 * @property id Unique identifier for the subcategory
 * @property categoryId ID of the parent category
 * @property name Name of the subcategory
 * @property description Optional description of the subcategory
 * @property priority Priority level of the subcategory (higher number means higher priority)
 * @property createdAt Timestamp when the subcategory was created
 * @property updatedAt Timestamp when the subcategory was last updated
 */
data class QuestionaryChecklistItemSubcategory(
    val id: String,
    val categoryId: String,
    val name: String,
    val description: String = "",
    val priority: Int = 5,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) 