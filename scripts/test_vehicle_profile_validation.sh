#!/bin/bash

# Test script for vehicle profile business/site validation
# This script monitors logs to verify that users cannot start checklists or sessions on vehicles outside their context

echo "🔍 Testing Vehicle Profile Business/Site Validation..."
echo "====================================================="

# Clear previous logs
adb logcat -c

echo "📱 Monitoring logs for vehicle profile validation..."
echo "   - Look for 'VehicleProfileVM' and 'VehicleProfileScreen' tags"
echo "   - Check for validation success/failure messages"
echo "   - Verify that Start Checklist option is hidden when vehicle is not in user context"
echo ""

# Monitor logs with relevant tags
adb logcat | grep -E "(VehicleProfileVM|VehicleProfileScreen|Validation)" --line-buffered 