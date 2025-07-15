package app.forku.data.repository.checklist

import app.forku.data.api.ChecklistItemSubcategoryApi
import app.forku.data.mapper.toDomain
import app.forku.domain.model.checklist.ChecklistItemSubcategory
import app.forku.domain.repository.checklist.ChecklistItemSubcategoryRepository
import android.util.Log
import javax.inject.Inject

class ChecklistItemSubcategoryRepositoryImpl @Inject constructor(
    private val api: ChecklistItemSubcategoryApi
) : ChecklistItemSubcategoryRepository {

    private val TAG = "ChecklistItemSubcategoryRepo"

    override suspend fun getAllSubcategories(): List<ChecklistItemSubcategory> {
        try {
            val response = api.getList()
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Fetched ${response.body()!!.size} subcategories")
                return response.body()!!.map { it.toDomain() }
            } else {
                throw Exception("Failed to fetch subcategories: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all subcategories", e)
            throw e
        }
    }

    override suspend fun getSubcategoriesByCategoryId(categoryId: String): List<ChecklistItemSubcategory> {
        try {
            val allSubcategories = getAllSubcategories()
            Log.d(TAG, "Filtering subcategories for categoryId: $categoryId")
            Log.d(TAG, "Available subcategories: ${allSubcategories.map { "${it.name} (categoryId: ${it.categoryId})" }}")
            val filteredSubcategories = allSubcategories.filter { it.categoryId == categoryId }
            Log.d(TAG, "Found ${filteredSubcategories.size} subcategories for category $categoryId")
            return filteredSubcategories
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching subcategories for category $categoryId", e)
            throw e
        }
    }

    override suspend fun getSubcategoryById(id: String): ChecklistItemSubcategory? {
        try {
            val response = api.getById(id)
            if (response.isSuccessful && response.body() != null) {
                return response.body()!!.toDomain()
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching subcategory by id: $id", e)
            return null
        }
    }
} 