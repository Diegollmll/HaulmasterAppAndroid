package app.forku.data.model

import com.google.gson.annotations.SerializedName

data class QuestionaryChecklistItemSubcategoryDto(
    @SerializedName("id")
    val id: String? = null,
    
    @SerializedName("categoryId")
    val categoryId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("priority")
    val priority: Int = 5,

) 