package app.forku.data.api.dto

import com.google.gson.annotations.SerializedName

data class QuestionaryChecklistItemDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("questionaryChecklistId")
    val questionaryChecklistId: String,

    @SerializedName("categoryId")
    val categoryId: String? = null,

    @SerializedName("subCategoryId")
    val subCategoryId: String? = null,

    @SerializedName("energySource")
    val energySource: List<String> = listOf("ALL"),

    @SerializedName("vehicleTypeList")
    val vehicleType: List<String> = listOf("ALL"),

    @SerializedName("componentId")
    val componentId: String? = null,

    @SerializedName("question")
    val question: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("isCritical")
    val isCritical: Boolean = false,

    @SerializedName("expectedAnswer")
    val expectedAnswer: String? = "PASS",

    @SerializedName("rotationGroup")
    val rotationGroup: Int = 1,

    @SerializedName("isActive")
    val isActive: Boolean = true,

    @SerializedName("position")
    val position: Int = 0,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("isRandomQuestion")
    val isRandomQuestion: Boolean = false,

    @SerializedName("checksUntilAppear")
    val checksUntilAppear: Int = 0,

    @SerializedName("maxChecksUntilAppear")
    val maxChecksUntilAppear: Int = 0

) 