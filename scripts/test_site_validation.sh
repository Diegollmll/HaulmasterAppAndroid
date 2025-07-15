#!/bin/bash

# Test script for business and site validation in QR scanner
# This script helps verify that users can only access vehicles from their assigned business and site

echo "🔍 Testing Business and Site Validation in QR Scanner..."
echo "========================================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "success")
            echo -e "${GREEN}✅ $message${NC}"
            ;;
        "error")
            echo -e "${RED}❌ $message${NC}"
            ;;
        "warning")
            echo -e "${YELLOW}⚠️  $message${NC}"
            ;;
        "info")
            echo -e "${BLUE}ℹ️  $message${NC}"
            ;;
    esac
}

# Check if app is running
echo "1. Checking if ForkU app is running..."
adb shell ps | grep -i forku
if [ $? -eq 0 ]; then
    print_status "success" "ForkU app is running"
else
    print_status "error" "ForkU app is not running"
    exit 1
fi

# Clear logs to start fresh
echo ""
echo "2. Clearing previous logs..."
adb logcat -c

# Monitor logs for context validation events
echo ""
echo "3. Starting log monitoring for context validation events..."
echo "   (This will capture the next 60 seconds of logs)"
echo ""

# Start monitoring in background
adb logcat -s "QRFlow:V" "UserPreferencesRepo:V" "BaseScreen:V" "SessionModalDebug:V" &
LOGCAT_PID=$!

# Wait for user to test
echo "📱 Please test the following scenarios:"
echo ""
echo "   SCENARIO 1: Scan QR of vehicle from YOUR assigned business and site"
echo "   - Expected: Should allow access to checklist"
echo "   - Look for: 'Vehicle context validation result: true'"
echo ""
echo "   SCENARIO 2: Scan QR of vehicle from OTHER business or site"
echo "   - Expected: Should show error message"
echo "   - Look for: 'Vehicle context validation result: false'"
echo "   - Look for: 'This vehicle does not belong to your assigned business and site'"
echo ""
echo "   SCENARIO 3: Check logs for validation details"
echo "   - Look for: 'Checking if vehicle belongs to user's assigned business and site'"
echo "   - Look for: 'User's business' and 'Vehicle's business'"
echo "   - Look for: 'User's site' and 'Vehicle's site'"
echo ""
echo "⏰ Monitoring logs for 60 seconds..."
sleep 60

# Stop log monitoring
kill $LOGCAT_PID 2>/dev/null

echo ""
echo "4. Analyzing captured logs..."

# Search for validation events
echo ""
echo "🔍 Context Validation Events:"
adb logcat -d | grep -E "(Vehicle context validation|Checking if vehicle belongs to user's assigned business and site|User's business|Vehicle's business|User's site|Vehicle's site)" | tail -10

echo ""
echo "🔍 QR Scanner Events:"
adb logcat -d | grep -E "(QRFlow.*onQrScanned|Vehicle retrieved.*businessId|Vehicle retrieved.*siteId)" | tail -10

echo ""
echo "🔍 Error Messages:"
adb logcat -d | grep -E "(does not belong to your assigned business and site|Vehicle context validation result: false)" | tail -5

echo ""
echo "📊 Summary:"
echo "==========="

# Count validation events
VALIDATION_COUNT=$(adb logcat -d | grep -c "Vehicle context validation result")
SUCCESS_COUNT=$(adb logcat -d | grep -c "Vehicle context validation result: true")
FAILURE_COUNT=$(adb logcat -d | grep -c "Vehicle context validation result: false")
ERROR_COUNT=$(adb logcat -d | grep -c "does not belong to your assigned business and site")

echo "Total validation events: $VALIDATION_COUNT"
echo "Successful validations (same business and site): $SUCCESS_COUNT"
echo "Failed validations (different business or site): $FAILURE_COUNT"
echo "Error messages shown: $ERROR_COUNT"

if [ $VALIDATION_COUNT -gt 0 ]; then
    if [ $FAILURE_COUNT -gt 0 ] && [ $ERROR_COUNT -gt 0 ]; then
        print_status "success" "Business and site validation is working correctly!"
        print_status "info" "Users are being blocked from accessing vehicles from other businesses or sites"
    else
        print_status "warning" "Business and site validation may not be working as expected"
        print_status "info" "No failed validations detected - make sure to test with vehicles from different businesses or sites"
    fi
else
    print_status "error" "No validation events detected"
    print_status "info" "Make sure to scan QR codes during the test period"
fi

echo ""
echo "🎯 Test completed!"
echo "If you see validation events and error messages for wrong businesses or sites, the feature is working correctly." 