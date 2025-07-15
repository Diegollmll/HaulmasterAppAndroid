# Hour Meter Validation System

## Overview
This system ensures that hour meter readings can only increase, never decrease, across all parts of the ForkU application. It provides both UI and business logic validation to maintain data integrity.

## Components

### 1. HourMeterValidator (Core Logic)
**Location**: `app/forku/core/validation/HourMeterValidator.kt`

Centralized validation logic with methods for different scenarios:

- `validateVehicleHourMeterUpdate()` - For manual vehicle hour meter updates
- `validateInitialHourMeter()` - For session start validation
- `validateFinalHourMeter()` - For session end validation
- `formatHourMeter()` - Consistent formatting
- `calculateHourMeterDifference()` - Calculate session duration

### 2. HourMeterIncrementer (UI Component)
**Location**: `app/forku/presentation/common/components/HourMeterIncrementer.kt`

Advanced UI component with features:
- ✅ Prevents decrementing below minimum value
- ✅ Quick increment buttons (+0.5h, +1h, +2h, +5h)
- ✅ Button-only input (read-only text field)
- ✅ Real-time validation feedback
- ✅ Visual error states
- ✅ Loading states
- ✅ Automatic formatting

### 3. HourMeterDialog (Enhanced)
**Location**: `app/forku/presentation/common/components/HourMeterDialog.kt`

Enhanced dialog with validation:
- ✅ Real-time validation
- ✅ Error messages
- ✅ Success feedback
- ✅ Configurable `allowEqual` parameter

### 4. Repository Validation
**Locations**: 
- `VehicleRepositoryImpl.updateCurrentHourMeter()`
- `VehicleSessionRepositoryImpl.startSession()`
- `VehicleSessionRepositoryImpl.endSession()`

Backend validation ensures data integrity even if UI validation is bypassed.

## Usage Examples

### Vehicle Hour Meter Update
```kotlin
// UI Component
HourMeterIncrementer(
    currentValue = vehicle.currentHourMeter,
    minValue = vehicle.currentHourMeter,
    onValueChange = { newValue -> viewModel.updateHourMeter(newValue) },
    title = "Vehicle Hour Meter",
    allowEqual = false // Must increase
)

// Dialog Version
HourMeterDialog(
    isVisible = showDialog,
    currentValue = vehicle.currentHourMeter,
    allowEqual = false, // Must increase
    onConfirm = { newValue -> viewModel.updateHourMeter(newValue) }
)
```

### Session Initial Hour Meter
```kotlin
HourMeterDialog(
    currentValue = vehicle.currentHourMeter,
    allowEqual = true, // Can equal vehicle's current reading
    onConfirm = { initialValue -> viewModel.startSession(initialValue) }
)
```

### Session Final Hour Meter
```kotlin
HourMeterDialog(
    currentValue = session.initialHourMeter,
    allowEqual = false, // Must be greater than initial
    onConfirm = { finalValue -> viewModel.endSession(finalValue) }
)
```

## Validation Rules

### 1. Vehicle Hour Meter Updates
- ❌ Cannot decrease from current value
- ❌ Cannot equal current value (must increase)
- ✅ Must be valid number format
- ✅ Must be positive

### 2. Session Initial Hour Meter
- ❌ Cannot be less than vehicle's current hour meter
- ✅ Can equal vehicle's current hour meter
- ✅ Must be valid number format
- ✅ Must be positive

### 3. Session Final Hour Meter
- ❌ Cannot be less than session's initial hour meter
- ❌ Cannot equal session's initial hour meter (must increase)
- ✅ Must be valid number format
- ✅ Must be positive

## Error Messages
The system provides clear, user-friendly error messages:

- "Hour meter cannot decrease. New value (X) is less than current value (Y)."
- "Hour meter value must increase. Current value is already X."
- "Invalid hour meter format: 'X'. Must be a valid number."
- "Hour meter cannot be negative: X"

## Integration Points

### ViewModels Updated
- ✅ `VehicleProfileViewModel` - Manual hour meter updates
- ✅ `ChecklistViewModel` - Initial hour meter for session start
- ✅ `VehicleProfileViewModel` - Final hour meter for session end

### Screens Updated
- ✅ `VehicleProfileScreen` - Both manual and session-end dialogs
- ✅ `ChecklistScreen` - Initial hour meter dialog

### Repositories Updated
- ✅ `VehicleRepositoryImpl` - Vehicle hour meter updates
- ✅ `VehicleSessionRepositoryImpl` - Session hour meter validation

## Localization Support

### Decimal Separator Handling
The system automatically handles different decimal separators:
- ✅ **Dot format**: `123.5` (English/US)
- ✅ **Comma format**: `123,5` (European/Spanish)
- ✅ **Integer format**: `123` (No decimals)
- ✅ **Mixed formats**: Can compare `123,5` with `123.0`

### Automatic Normalization
All input values are automatically normalized to dot format for internal processing:
- Input: `"2,5"` → Parsed as: `2.5` → Stored as: `"2.5"`
- Input: `"2.5"` → Parsed as: `2.5` → Stored as: `"2.5"`

## Benefits

### 1. Data Integrity
- Prevents impossible hour meter readings
- Ensures consistent data across the system
- Maintains audit trail accuracy
- Handles localized input formats

### 2. User Experience
- Clear validation feedback
- Intuitive increment controls
- Prevents user errors
- Supports regional number formats

### 3. Business Logic
- Accurate session duration calculations
- Reliable maintenance scheduling
- Proper vehicle usage tracking
- Cross-locale compatibility

## Testing

### Manual Testing Scenarios
1. Try to decrease vehicle hour meter → Should show error
2. Try to set initial hour meter below vehicle's current → Should show error
3. Try to set final hour meter below initial → Should show error
4. Use quick increment buttons → Should work smoothly
5. Enter invalid formats → Should show format error
6. **NEW**: Enter comma format (`2,5`) → Should accept and normalize
7. **NEW**: Enter dot format (`2.5`) → Should accept
8. **NEW**: Mix formats (current: `100,0`, new: `101.5`) → Should work

### Edge Cases Handled
- Empty/null values
- Non-numeric input
- Negative values
- Very large values
- Decimal precision

## Future Enhancements

### Possible Improvements
1. **Bulk Hour Meter Updates** - For multiple vehicles
2. **Hour Meter History** - Track all changes with timestamps
3. **Maintenance Alerts** - Based on hour meter thresholds
4. **Predictive Validation** - Warn about unusually large increases
5. **Import/Export** - Hour meter data management

### Configuration Options
1. **Decimal Places** - Configurable precision
2. **Increment Steps** - Customizable quick buttons
3. **Validation Strictness** - Different rules per vehicle type
4. **Auto-formatting** - Optional formatting preferences

## Troubleshooting

### Common Issues
1. **Validation Too Strict** - Check `allowEqual` parameter
2. **Format Errors** - Ensure proper decimal formatting
3. **Repository Errors** - Check backend validation logs
4. **UI Not Updating** - Verify ViewModel state management

### Debug Logging
All validation operations include detailed logging with tag patterns:
- `HourMeterValidator` - Core validation logic
- `VehicleRepositoryImpl` - Repository operations
- `VehicleSessionRepo` - Session operations 