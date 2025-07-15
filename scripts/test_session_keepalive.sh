#!/bin/bash

# Test script for "Seguir trabajando" button functionality
# This simulates the exact flow that happens when the user clicks the button

echo "🔍 Testing 'Seguir trabajando' button functionality..."
echo "=================================================="

# 1. Check if app is running
echo "1. Checking if ForkU app is running..."
adb shell ps | grep -i forku
if [ $? -eq 0 ]; then
    echo "✅ ForkU app is running"
else
    echo "❌ ForkU app is not running"
    exit 1
fi

# 2. Clear logs to start fresh
echo ""
echo "2. Clearing previous logs..."
adb logcat -c

# 3. Monitor logs for session modal and button click
echo ""
echo "3. Starting log monitoring for session modal events..."
echo "   (This will capture the next 60 seconds of logs)"
echo ""

# Monitor for specific log patterns related to the session modal
adb logcat | grep -E "(Session Modal|BaseScreen.*Session Modal|performKeepAlive|performTokenRenewal|TokenRenewal result|KeepAlive result|signalAuthenticationRequired|Authentication required)" &

# Store the background process ID
LOG_PID=$!

# 4. Wait for user to trigger the modal
echo ""
echo "4. 🔄 Waiting for session modal to appear..."
echo "   Please navigate to a screen that shows the session modal"
echo "   and click 'Seguir trabajando' button"
echo ""
echo "   The script will monitor for 60 seconds..."
echo ""

# Wait 60 seconds for the user to trigger the modal
sleep 60

# 5. Stop log monitoring
echo ""
echo "5. Stopping log monitoring..."
kill $LOG_PID 2>/dev/null

# 6. Get the captured logs
echo ""
echo "6. Analyzing captured logs..."
echo "=================================================="

# Get logs from the last 60 seconds and filter for relevant events
adb logcat -d | grep -E "(Session Modal|BaseScreen.*Session Modal|performKeepAlive|performTokenRenewal|TokenRenewal result|KeepAlive result|signalAuthenticationRequired|Authentication required|SessionKeepAlive|Token renewal|Keep-alive)" | tail -50

echo ""
echo "=================================================="
echo "📊 Analysis Summary:"
echo ""

# Check for specific patterns
echo "🔍 Looking for key events..."

# Check if session modal appeared
if adb logcat -d | grep -q "Session Modal"; then
    echo "✅ Session modal was triggered"
else
    echo "❌ No session modal events found"
fi

# Check if keep-alive was performed
if adb logcat -d | grep -q "performKeepAlive"; then
    echo "✅ Keep-alive was performed"
else
    echo "❌ No keep-alive events found"
fi

# Check if token renewal was attempted
if adb logcat -d | grep -q "performTokenRenewal"; then
    echo "✅ Token renewal was attempted"
else
    echo "❌ No token renewal events found"
fi

# Check for authentication required signals
if adb logcat -d | grep -q "signalAuthenticationRequired"; then
    echo "⚠️  Authentication required was signaled (this may indicate failure)"
else
    echo "✅ No authentication required signals (good)"
fi

# Check for successful token renewal
if adb logcat -d | grep -q "Token renewed and stored successfully"; then
    echo "✅ Token renewal was successful"
else
    echo "❌ No successful token renewal found"
fi

# Check for session restart
if adb logcat -d | grep -q "Reiniciando keep-alive"; then
    echo "✅ Keep-alive was restarted after renewal"
else
    echo "❌ No keep-alive restart found"
fi

echo ""
echo "🎯 Recommendations:"
echo ""

# Provide recommendations based on findings
if adb logcat -d | grep -q "signalAuthenticationRequired"; then
    echo "❌ ISSUE DETECTED: The 'Seguir trabajando' button is triggering authentication required"
    echo "   This means the token renewal is failing and the user is being redirected to login"
    echo ""
    echo "🔧 Possible fixes:"
    echo "   1. Check if the token is actually expired"
    echo "   2. Verify the renewToken API endpoint is working"
    echo "   3. Check if the CSRF token is valid"
    echo "   4. Ensure the authentication token is available"
else
    echo "✅ No immediate issues detected"
    echo "   The session modal and button appear to be working correctly"
fi

echo ""
echo "📝 Next steps:"
echo "   1. Review the detailed logs above"
echo "   2. Check if token renewal API is responding correctly"
echo "   3. Verify session keep-alive is maintaining the session"
echo "   4. Test with different session expiration scenarios"

echo ""
echo "�� Test completed!" 