#!/bin/bash

# Test script specifically for "Seguir trabajando" button
# This script helps verify that the button properly maintains the session

echo "🔍 Testing 'Seguir trabajando' button functionality..."
echo "=================================================="

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

# 1. Check if app is running
print_status "info" "Checking if ForkU app is running..."
if adb shell ps | grep -i forku > /dev/null; then
    print_status "success" "ForkU app is running"
else
    print_status "error" "ForkU app is not running"
    echo "Please start the ForkU app first"
    exit 1
fi

# 2. Clear logs to start fresh
print_status "info" "Clearing previous logs..."
adb logcat -c

# 3. Instructions for user
echo ""
echo "📋 TEST INSTRUCTIONS:"
echo "===================="
echo "1. Navigate to any screen in the app"
echo "2. Wait for the session modal to appear (or trigger it manually)"
echo "3. Click the 'Seguir trabajando' button"
echo "4. The script will monitor the logs for 30 seconds"
echo ""

# 4. Start monitoring logs
print_status "info" "Starting log monitoring for 'Seguir trabajando' button..."
echo "Monitoring will continue for 30 seconds..."
echo ""

# Create a temporary log file
TEMP_LOG="/tmp/seguir_trabajando_test.log"

# Monitor logs in background
adb logcat | grep -E "(Session Modal|BaseScreen.*Session Modal|performKeepAlive|performTokenRenewal|TokenRenewal result|KeepAlive result|signalAuthenticationRequired|Authentication required|SessionKeepAlive|Token renewal|Keep-alive|🔄|✅|❌|⚠️|💀|💥)" > "$TEMP_LOG" &

LOG_PID=$!

# 5. Wait for user action
echo "⏳ Waiting for you to click 'Seguir trabajando'..."
echo "You have 30 seconds to trigger the button..."
echo ""

# Countdown
for i in {30..1}; do
    echo -ne "\r⏰ Time remaining: ${i}s"
    sleep 1
done
echo ""

# 6. Stop log monitoring
print_status "info" "Stopping log monitoring..."
kill $LOG_PID 2>/dev/null

# 7. Analyze results
echo ""
echo "📊 ANALYSIS RESULTS:"
echo "==================="

# Check for specific patterns
echo ""
print_status "info" "Checking for key events..."

# Check if session modal was triggered
if grep -q "Session Modal" "$TEMP_LOG"; then
    print_status "success" "Session modal was triggered"
else
    print_status "warning" "No session modal events found"
fi

# Check if session maintenance started
if grep -q "🔄 Starting session maintenance" "$TEMP_LOG"; then
    print_status "success" "Session maintenance process started"
else
    print_status "warning" "No session maintenance start found"
fi

# Check if token renewal was attempted
if grep -q "performTokenRenewal" "$TEMP_LOG"; then
    print_status "success" "Token renewal was attempted"
else
    print_status "warning" "No token renewal attempts found"
fi

# Check if keep-alive was attempted
if grep -q "performKeepAlive" "$TEMP_LOG"; then
    print_status "success" "Keep-alive was attempted"
else
    print_status "warning" "No keep-alive attempts found"
fi

# Check for successful token renewal
if grep -q "✅ Token renewed successfully" "$TEMP_LOG"; then
    print_status "success" "Token renewal was successful"
else
    print_status "warning" "No successful token renewal found"
fi

# Check for successful keep-alive as fallback
if grep -q "✅ Keep-alive successful as fallback" "$TEMP_LOG"; then
    print_status "success" "Keep-alive worked as fallback"
else
    print_status "info" "No keep-alive fallback needed"
fi

# Check for authentication required signals
if grep -q "signalAuthenticationRequired" "$TEMP_LOG"; then
    print_status "error" "Authentication required was signaled (session maintenance failed)"
else
    print_status "success" "No authentication required signals (session maintained)"
fi

# Check for session restart
if grep -q "Restarting keep-alive" "$TEMP_LOG"; then
    print_status "success" "Keep-alive was restarted after maintenance"
else
    print_status "warning" "No keep-alive restart found"
fi

# Check for errors
if grep -q "💀 Both renewal and keep-alive failed" "$TEMP_LOG"; then
    print_status "error" "Both token renewal and keep-alive failed"
elif grep -q "💥 Exception during session maintenance" "$TEMP_LOG"; then
    print_status "error" "Exception occurred during session maintenance"
fi

# 8. Show detailed logs
echo ""
echo "📝 DETAILED LOGS:"
echo "================"
if [ -s "$TEMP_LOG" ]; then
    cat "$TEMP_LOG"
else
    print_status "warning" "No relevant logs captured"
fi

# 9. Summary and recommendations
echo ""
echo "🎯 SUMMARY:"
echo "=========="

# Determine overall status
if grep -q "signalAuthenticationRequired" "$TEMP_LOG"; then
    print_status "error" "ISSUE DETECTED: Session maintenance failed"
    echo ""
    echo "🔧 Possible causes:"
    echo "   • Token is actually expired"
    echo "   • Network connectivity issues"
    echo "   • Server-side problems with token renewal"
    echo "   • CSRF token issues"
    echo ""
    echo "🔧 Recommended fixes:"
    echo "   • Check network connectivity"
    echo "   • Verify server is responding"
    echo "   • Check if user needs to re-authenticate"
    echo "   • Review server logs for token renewal errors"
elif grep -q "✅ Token renewed successfully\|✅ Keep-alive successful as fallback" "$TEMP_LOG"; then
    print_status "success" "SUCCESS: Session maintenance worked correctly"
    echo ""
    echo "✅ The 'Seguir trabajando' button is working as expected"
    echo "✅ The session was maintained successfully"
    echo "✅ No redirection to login occurred"
else
    print_status "warning" "INCONCLUSIVE: Need more testing"
    echo ""
    echo "⚠️  The test didn't capture enough information"
    echo "⚠️  Try running the test again"
    echo "⚠️  Make sure the session modal appears"
fi

# 10. Cleanup
rm -f "$TEMP_LOG"

echo ""
echo "🏁 Test completed!"
echo ""
echo "📝 Next steps:"
echo "   1. Review the detailed logs above"
echo "   2. If issues persist, check server logs"
echo "   3. Verify network connectivity"
echo "   4. Test with different session states" 