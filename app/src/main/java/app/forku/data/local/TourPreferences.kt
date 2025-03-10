package app.forku.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TourPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasTourCompleted(): Boolean {
        return prefs.getBoolean(KEY_TOUR_COMPLETED, false)
    }

    fun setTourCompleted() {
        prefs.edit().putBoolean(KEY_TOUR_COMPLETED, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "tour_preferences"
        private const val KEY_TOUR_COMPLETED = "tour_completed"
    }
} 