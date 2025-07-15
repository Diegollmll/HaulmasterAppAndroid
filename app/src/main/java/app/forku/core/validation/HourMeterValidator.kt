package app.forku.core.validation

import android.util.Log

/**
 * Hour Meter Validator
 * Centralized validation logic for hour meter readings
 * Ensures hour meter values can only increase, never decrease
 */
object HourMeterValidator {
    
    private const val TAG = "HourMeterValidator"
    
    /**
     * Validation result for hour meter operations
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val validatedValue: Double? = null
    )
    
    /**
     * Validate a new hour meter reading against the current reading
     * 
     * @param newValue The new hour meter reading to validate
     * @param currentValue The current hour meter reading (minimum allowed)
     * @param allowEqual Whether equal values are allowed (default: true)
     * @return ValidationResult with validation status and details
     */
    fun validateHourMeterUpdate(
        newValue: String?,
        currentValue: String?,
        allowEqual: Boolean = true
    ): ValidationResult {
        Log.d(TAG, "🔍 Validating hour meter update: new='$newValue', current='$currentValue', allowEqual=$allowEqual")
        
        // Parse new value with localization support
        val newDouble = parseHourMeterValue(newValue)
        if (newDouble == null) {
            val error = "Invalid hour meter format: '$newValue'. Must be a valid number."
            Log.w(TAG, "❌ $error")
            return ValidationResult(false, error)
        }
        
        // Parse current value with localization support
        val currentDouble = parseHourMeterValue(currentValue) ?: 0.0
        
        // Validate range
        if (newDouble < 0) {
            val error = "Hour meter cannot be negative: $newDouble"
            Log.w(TAG, "❌ $error")
            return ValidationResult(false, error)
        }
        
        // Validate against current value (prevent decrementing)
        when {
            newDouble < currentDouble -> {
                val error = "Hour meter cannot decrease. New value ($newDouble) is less than current value ($currentDouble)."
                Log.w(TAG, "❌ $error")
                return ValidationResult(false, error)
            }
            newDouble == currentDouble && !allowEqual -> {
                val error = "Hour meter value must increase. Current value is already $currentDouble."
                Log.w(TAG, "❌ $error")
                return ValidationResult(false, error)
            }
            else -> {
                Log.d(TAG, "✅ Hour meter validation passed: $newDouble >= $currentDouble")
                return ValidationResult(true, validatedValue = newDouble)
            }
        }
    }
    
    /**
     * Validate initial hour meter reading (for session start)
     * 
     * @param initialValue The initial hour meter reading
     * @param vehicleCurrentValue The vehicle's current hour meter reading
     * @return ValidationResult with validation status and details
     */
    fun validateInitialHourMeter(
        initialValue: String?,
        vehicleCurrentValue: String?
    ): ValidationResult {
        Log.d(TAG, "🔍 Validating initial hour meter: initial='$initialValue', vehicleCurrent='$vehicleCurrentValue'")
        
        return validateHourMeterUpdate(
            newValue = initialValue,
            currentValue = vehicleCurrentValue,
            allowEqual = true // Allow equal values for initial reading
        )
    }
    
    /**
     * Validate final hour meter reading (for session end)
     * 
     * @param finalValue The final hour meter reading
     * @param initialValue The initial hour meter reading from session start
     * @return ValidationResult with validation status and details
     */
    fun validateFinalHourMeter(
        finalValue: String?,
        initialValue: String?
    ): ValidationResult {
        Log.d(TAG, "🔍 Validating final hour meter: final='$finalValue', initial='$initialValue'")
        
        return validateHourMeterUpdate(
            newValue = finalValue,
            currentValue = initialValue,
            allowEqual = false // Final reading must be greater than initial
        )
    }
    
    /**
     * Validate vehicle hour meter update (manual update)
     * 
     * @param newValue The new hour meter reading
     * @param currentValue The vehicle's current hour meter reading
     * @return ValidationResult with validation status and details
     */
    fun validateVehicleHourMeterUpdate(
        newValue: String?,
        currentValue: String?
    ): ValidationResult {
        Log.d(TAG, "🔍 Validating vehicle hour meter update: new='$newValue', current='$currentValue'")
        
        return validateHourMeterUpdate(
            newValue = newValue,
            currentValue = currentValue,
            allowEqual = false // Vehicle hour meter updates must increase
        )
    }
    
    /**
     * Format hour meter value to consistent decimal places
     * 
     * @param value The hour meter value to format
     * @param decimalPlaces Number of decimal places (default: 1)
     * @return Formatted hour meter string
     */
    fun formatHourMeter(value: Double, decimalPlaces: Int = 1): String {
        return String.format("%.${decimalPlaces}f", value)
    }
    
    /**
     * Format hour meter value from string
     * 
     * @param value The hour meter value string to format
     * @param decimalPlaces Number of decimal places (default: 1)
     * @return Formatted hour meter string or original if invalid
     */
    fun formatHourMeter(value: String?, decimalPlaces: Int = 1): String {
        val double = parseHourMeterValue(value)
        return if (double != null) {
            formatHourMeter(double, decimalPlaces)
        } else {
            value ?: "0.0"
        }
    }
    
    /**
     * Calculate hour meter difference
     * 
     * @param finalValue The final hour meter reading
     * @param initialValue The initial hour meter reading
     * @return Hour meter difference or null if invalid values
     */
    fun calculateHourMeterDifference(
        finalValue: String?,
        initialValue: String?
    ): Double? {
        val final = parseHourMeterValue(finalValue)
        val initial = parseHourMeterValue(initialValue)
        
        return if (final != null && initial != null && final >= initial) {
            final - initial
        } else {
            null
        }
    }
    
    /**
     * Check if hour meter value is valid format
     * 
     * @param value The hour meter value to check
     * @return True if valid format, false otherwise
     */
    fun isValidFormat(value: String?): Boolean {
        return parseHourMeterValue(value)?.let { it >= 0 } == true
    }
    
    /**
     * Parse hour meter value handling different decimal separators
     * Supports both comma (,) and dot (.) as decimal separators
     * 
     * @param value The hour meter value string to parse
     * @return Parsed double value or null if invalid
     */
    private fun parseHourMeterValue(value: String?): Double? {
        if (value.isNullOrBlank()) return null
        
        return try {
            // First try standard parsing (dot as decimal separator)
            val dotResult = value.toDoubleOrNull()
            if (dotResult != null) {
                Log.d(TAG, "✅ Parsed '$value' as $dotResult (dot format)")
                return dotResult
            }
            
            // If that fails, try replacing comma with dot (for localized input)
            val normalizedValue = value.replace(',', '.')
            val commaResult = normalizedValue.toDoubleOrNull()
            if (commaResult != null) {
                Log.d(TAG, "✅ Parsed '$value' as $commaResult (comma format normalized to '$normalizedValue')")
                return commaResult
            }
            
            Log.w(TAG, "❌ Failed to parse hour meter value: '$value'")
            null
        } catch (e: Exception) {
            Log.w(TAG, "❌ Exception parsing hour meter value: '$value'", e)
            null
        }
    }
} 