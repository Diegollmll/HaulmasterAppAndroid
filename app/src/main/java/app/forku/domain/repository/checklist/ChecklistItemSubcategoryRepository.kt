package app.forku.domain.repository.checklist

import app.forku.domain.model.checklist.ChecklistItemSubcategory

interface ChecklistItemSubcategoryRepository {
    suspend fun getAllSubcategories(): List<ChecklistItemSubcategory>
    suspend fun getSubcategoriesByCategoryId(categoryId: String): List<ChecklistItemSubcategory>
    suspend fun getSubcategoryById(id: String): ChecklistItemSubcategory?
} 