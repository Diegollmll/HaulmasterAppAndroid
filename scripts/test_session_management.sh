#!/bin/bash

# Test script for session management and token expiration detection
# This script helps verify that the session keep-alive and expiration modal are working

echo "🧪 Testing Session Management and Token Expiration Detection"
echo "=========================================================="

# Check if adb is available
if ! command -v adb &> /dev/null; then
    echo "❌ ADB not found. Please install Android SDK and add it to PATH"
    exit 1
fi

# Check if device is connected
if ! adb devices | grep -q "device$"; then
    echo "❌ No Android device connected. Please connect a device or start an emulator"
    exit 1
fi

echo "✅ Device connected: $(adb devices | grep 'device$' | head -1 | cut -f1)"

# Function to check logs for session management
check_session_logs() {
    echo ""
    echo "🔍 Checking Session Management Logs..."
    echo "-------------------------------------"
    
    # Check for SessionKeepAlive logs
    echo "📊 SessionKeepAlive Status:"
    adb logcat -d | grep "SessionKeepAlive" | tail -10 | while read line; do
        echo "  $line"
    done
    
    # Check for BaseScreen session logs
    echo ""
    echo "📊 BaseScreen Session Status:"
    adb logcat -d | grep "BaseScreen.*session\|BaseScreen.*Session" | tail -10 | while read line; do
        echo "  $line"
    done
    
    # Check for AuthInterceptor logs
    echo ""
    echo "📊 AuthInterceptor Status:"
    adb logcat -d | grep "appflow AuthInterceptor" | tail -10 | while read line; do
        echo "  $line"
    done
    
    # Check for token expiration logs
    echo ""
    echo "📊 Token Expiration Detection:"
    adb logcat -d | grep -i "expir\|token.*expir\|session.*expir" | tail -10 | while read line; do
        echo "  $line"
    done
    
    # Check for modal logs
    echo ""
    echo "📊 Session Expiration Modal:"
    adb logcat -d | grep -i "modal\|session.*expir.*modal\|showSessionExpiringModal" | tail -10 | while read line; do
        echo "  $line"
    done
}

# Function to monitor logs in real-time
monitor_session_logs() {
    echo ""
    echo "🔍 Monitoring Session Management Logs (Press Ctrl+C to stop)..."
    echo "-------------------------------------------------------------"
    
    adb logcat -c  # Clear logs first
    
    # Monitor relevant logs
    adb logcat | grep -E "(SessionKeepAlive|BaseScreen.*session|appflow AuthInterceptor|token.*expir|session.*expir|modal)" | while read line; do
        timestamp=$(date '+%H:%M:%S')
        echo "[$timestamp] $line"
    done
}

# Function to test session keep-alive manually
test_keep_alive() {
    echo ""
    echo "🧪 Testing Session Keep-Alive..."
    echo "-------------------------------"
    
    # Clear logs
    adb logcat -c
    
    echo "📱 Please navigate to any screen in the app to trigger session management..."
    echo "⏳ Waiting 5 seconds for logs to appear..."
    sleep 5
    
    # Check for keep-alive logs
    echo "📊 Keep-Alive Logs:"
    adb logcat -d | grep "SessionKeepAlive\|keep.*alive\|KeepAlive" | tail -10 | while read line; do
        echo "  $line"
    done
}

# Function to test token expiration simulation
test_token_expiration() {
    echo ""
    echo "🧪 Testing Token Expiration Detection..."
    echo "---------------------------------------"
    
    echo "📱 Please try to perform an action that might trigger token expiration..."
    echo "   (e.g., wait for session to expire, or trigger an API call with expired token)"
    echo "⏳ Waiting 10 seconds for logs to appear..."
    sleep 10
    
    # Check for expiration logs
    echo "📊 Token Expiration Logs:"
    adb logcat -d | grep -i "expir\|token.*expir\|session.*expir\|401\|403" | tail -15 | while read line; do
        echo "  $line"
    done
}

# Main menu
while true; do
    echo ""
    echo "🎯 Session Management Test Menu"
    echo "=============================="
    echo "1. Check current session logs"
    echo "2. Monitor session logs in real-time"
    echo "3. Test session keep-alive"
    echo "4. Test token expiration detection"
    echo "5. Run all tests"
    echo "6. Exit"
    echo ""
    read -p "Select an option (1-6): " choice
    
    case $choice in
        1)
            check_session_logs
            ;;
        2)
            monitor_session_logs
            ;;
        3)
            test_keep_alive
            ;;
        4)
            test_token_expiration
            ;;
        5)
            check_session_logs
            test_keep_alive
            test_token_expiration
            ;;
        6)
            echo "👋 Exiting session management test"
            exit 0
            ;;
        *)
            echo "❌ Invalid option. Please select 1-6"
            ;;
    esac
done 