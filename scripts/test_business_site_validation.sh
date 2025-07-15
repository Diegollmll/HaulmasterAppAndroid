#!/bin/bash

# Test script for business and site validation functionality
# This script monitors logs to verify that users can only access vehicles from their assigned business and site

echo "🔍 Starting business and site validation test..."
echo "================================================"

# Clear previous logs
adb logcat -c

echo "📱 Monitoring logs for context validation..."
echo "   - Look for 'QRFlow' and 'UserPreferencesRepo' tags"
echo "   - Check for validation success/failure messages"
echo "   - Verify error messages when vehicle business/site doesn't match user context"
echo ""

# Monitor logs with relevant tags
adb logcat | grep -E "(QRFlow|UserPreferencesRepo|ContextValidation)" --line-buffered 