# SessionKeepAlive Testing Guide

## 🚀 Quick Testing Instructions

### Prerequisites
- Android device/emulator connected and running
- `adb` available in PATH
- ForkU project compiled successfully

### 1. Quick Test (Recommended)
```bash
# Install app and watch logs immediately
./scripts/test_session_keepalive.sh run
```

### 2. Step-by-Step Testing
```bash
# Step 1: Install the app
./scripts/test_session_keepalive.sh install

# Step 2: Watch logs in real-time
./scripts/test_session_keepalive.sh logs

# Step 3: In another terminal, start the app
adb shell am start -n app.forku/.MainActivity
```

### 3. Troubleshooting Test
```bash
# Clear app data and restart fresh
./scripts/test_session_keepalive.sh clear
```

## 📊 What to Look For

### ✅ Expected Successful Logs (in order):
1. **Authentication & Startup**:
   ```
   MainActivity: 🚀 User authenticated, starting session keep-alive
   SessionKeepAlive: 🚀 Starting session keep-alive service
   SessionKeepAlive: ✅ Keep-alive status set to active
   SessionKeepAlive: 🔄 Keep-alive coroutine started
   SessionKeepAlive: 🔄 Token renewal coroutine started
   SessionKeepAlive: 🎯 Session keep-alive service fully started
   ```

2. **Testing Execution (after 7 seconds)**:
   ```
   MainActivity: 🧪 TESTING: Forcing immediate execution...
   SessionKeepAlive: 🚀 FORCING IMMEDIATE EXECUTION FOR TESTING
   SessionKeepAlive: 🔄 Executing immediate keep-alive...
   SessionKeepAlive: ✅ Immediate keep-alive completed
   SessionKeepAlive: 🔄 Executing immediate token renewal...
   SessionKeepAlive: ✅ Immediate token renewal completed
   ```

3. **Regular Intervals**:
   - Keep-alive every **30 seconds**
   - Token renewal every **60 seconds**

### ❌ Problem Indicators:
- `🛑 Stopping session keep-alive service` (right after starting)
- `❌ Error in token renewal loop`
- `Job was cancelled` errors
- No logs appearing after startup

## ⚡ Current Testing Configuration

### Intervals (Reduced for Testing):
- **Keep-alive**: 30 seconds (production: 5 minutes)
- **Token renewal**: 60 seconds (production: 25 minutes)
- **Testing force execution**: 7 seconds after startup

### Key Features:
- **Mutex synchronization**: Prevents concurrent requests
- **Fresh CSRF tokens**: Force refresh for token renewal
- **Comprehensive logging**: Easy debugging with emojis
- **Automatic testing**: Force execution 7 seconds after login

## 🔧 Testing Scenarios

### Scenario 1: Fresh Install
```bash
./scripts/test_session_keepalive.sh clear
# Login with credentials
# Watch for successful startup sequence
```

### Scenario 2: Background/Foreground
```bash
# Start app and login
# Put app in background (home button)
# Wait 2 minutes
# Return to app
# Check if SessionKeepAlive continued running
```

### Scenario 3: Network Issues
```bash
# Start app and login
# Turn off WiFi/data
# Watch for error handling
# Turn on network
# Check if service recovers
```

## 📱 Manual Testing Steps

1. **Login**: Use valid credentials
2. **Wait 7 seconds**: Look for testing force execution
3. **Wait 30 seconds**: Look for first automatic keep-alive
4. **Wait 60 seconds**: Look for first automatic token renewal
5. **Navigate**: Change screens, ensure service continues
6. **Background**: Put app in background, ensure service continues

## 🛠️ Debugging Commands

### Check Current Status:
```bash
./scripts/test_session_keepalive.sh status
```

### Watch Only SessionKeepAlive Logs:
```bash
adb logcat | grep SessionKeepAlive
```

### Check App Process:
```bash
adb shell ps | grep app.forku
```

### Clear Logs and Start Fresh:
```bash
adb logcat -c
./scripts/test_session_keepalive.sh logs
```

## 🔄 Expected Timeline

| Time | Event | Expected Log |
|------|-------|--------------|
| 0s | Login | `🚀 User authenticated, starting session keep-alive` |
| 2s | Status Check | `📊 === SESSION KEEP-ALIVE STATUS ===` |
| 7s | Force Test | `🧪 TESTING: Forcing immediate execution...` |
| 30s | Keep-alive | `🔄 Performing keep-alive ping...` |
| 60s | Token Renewal | `Performing token renewal...` |
| 90s | Keep-alive | `🔄 Performing keep-alive ping...` |
| 120s | Token Renewal | `Performing token renewal...` |

## 🚨 Common Issues & Solutions

### Issue: Service stops immediately
**Symptoms**: `🛑 Stopping session keep-alive service` right after starting
**Solution**: Check if app is going to background or being paused

### Issue: No logs appearing
**Symptoms**: No SessionKeepAlive logs after login
**Solution**: 
1. Check if user is actually authenticated
2. Verify app is running: `adb shell ps | grep app.forku`
3. Clear app data and restart

### Issue: HTTP 403 errors
**Symptoms**: `⚠️ Token renewal failed: 403`
**Solution**: CSRF token issues - check if fresh tokens are being used

### Issue: HTTP 500 errors
**Symptoms**: `⚠️ Keep-alive failed: 500`
**Solution**: Backend authentication issues - check token validity

## 📝 Notes

- **Testing intervals**: Currently set to 30s/60s for faster testing
- **Production intervals**: Should be 5min/25min for production
- **Force execution**: Only active during testing phase
- **Mutex protection**: Prevents race conditions between requests
- **Fresh CSRF**: Token renewal uses force-refreshed CSRF tokens
