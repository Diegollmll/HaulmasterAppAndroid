package app.forku.data.api.dto

import com.google.gson.annotations.SerializedName

data class QuestionaryChecklistDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("isDefault")
    val isDefault: Boolean = false,

    @SerializedName("businessId")
    val businessId: String? = null,

    @SerializedName("siteId")
    val siteId: String? = null,

    @SerializedName("QuestionaryChecklistMetadata")
    val metadata: QuestionaryChecklistMetadataDto? = null,

    @SerializedName("QuestionaryChecklistRotationRules")
    val rotationRules: QuestionaryChecklistRotationRulesDto? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("items")
    val items: List<QuestionaryChecklistItemDto> = emptyList()

)

data class QuestionaryChecklistMetadataDto(
    @SerializedName("version")
    val version: String? = null,
    
    @SerializedName("lastUpdated")
    val lastUpdated: String? = null,
    
    @SerializedName("totalQuestions")
    val totalQuestions: Int = 0,
    
    @SerializedName("rotationGroups")
    val rotationGroups: Int = 0,
    
    @SerializedName("criticalityLevels")
    val criticalityLevels: List<String> = emptyList(),
    
    @SerializedName("energySources")
    val energySources: List<String> = emptyList(),
    
    @SerializedName("vehicleTypes")
    val vehicleTypes: List<String> = listOf("ALL")
)

data class QuestionaryChecklistRotationRulesDto(
    @SerializedName("maxQuestionsPerCheck")
    val maxQuestionsPerCheck: Int = 0,
    
    @SerializedName("requiredCategories")
    val requiredCategories: List<String> = emptyList(),
    
    @SerializedName("criticalQuestionMinimum")
    val criticalQuestionMinimum: Int = 0,
    
    @SerializedName("standardQuestionMaximum")
    val standardQuestionMaximum: Int = 0
) 