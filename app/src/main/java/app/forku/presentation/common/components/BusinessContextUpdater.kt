package app.forku.presentation.common.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.forku.core.business.BusinessContextManager
import app.forku.domain.repository.user.UserPreferencesRepository
import kotlinx.coroutines.launch
import android.util.Log

/**
 * Interface for ViewModels that can update business context
 * This centralizes the context update functionality
 */
interface BusinessContextUpdater {
    val businessContextManager: BusinessContextManager
    val userPreferencesRepository: UserPreferencesRepository
    
    /**
     * Reload data after context change - to be implemented by each ViewModel
     */
    fun reloadData()
}

/**
 * Extension function to update business context
 * This centralizes the business context update logic
 */
fun <T> T.updateBusinessContext(businessId: String) where T : ViewModel, T : BusinessContextUpdater {
    viewModelScope.launch {
        try {
            Log.d("BusinessContextUpdater", "Updating business context to: $businessId")
            
            // Update user preferences permanently
            userPreferencesRepository.updateDefaultBusinessId(businessId)
            userPreferencesRepository.updateLastSelectedBusinessId(businessId)
            
            // Update BusinessContextManager
            businessContextManager.setCurrentBusinessId(businessId)
            businessContextManager.setDefaultBusinessId(businessId)
            
            // Reload data with new context
            reloadData()
            
        } catch (e: Exception) {
            Log.e("BusinessContextUpdater", "Error updating business context: ${e.message}", e)
            throw e // Let the ViewModel handle the error display
        }
    }
}

/**
 * Extension function to update site context
 * This centralizes the site context update logic
 */
fun <T> T.updateSiteContext(siteId: String) where T : ViewModel, T : BusinessContextUpdater {
    viewModelScope.launch {
        try {
            Log.d("BusinessContextUpdater", "Updating site context to: $siteId")
            
            // Update user preferences permanently
            userPreferencesRepository.updateDefaultSiteId(siteId)
            userPreferencesRepository.updateLastSelectedSiteId(siteId)
            
            // Update BusinessContextManager
            businessContextManager.setCurrentSiteId(siteId)
            businessContextManager.setDefaultSiteId(siteId)
            
            // Reload data with new context
            reloadData()
            
        } catch (e: Exception) {
            Log.e("BusinessContextUpdater", "Error updating site context: ${e.message}", e)
            throw e // Let the ViewModel handle the error display
        }
    }
} 