#!/bin/bash

# Test script for Admin vehicle list initial loading with site filters
# This script monitors logs to verify that Admin users see vehicles from their assigned site on first entry

echo "🔍 Testing Admin Vehicle List Initial Loading..."
echo "================================================"

# Clear previous logs
adb logcat -c

echo "📱 Monitoring logs for Admin vehicle list loading..."
echo "   - Look for 'VehicleListScreen' and 'VehicleListViewModel' tags"
echo "   - Check for filter configuration and vehicle loading events"
echo "   - Verify that vehicles load from the correct site on first entry"
echo ""

# Monitor logs with relevant tags
adb logcat | grep -E "(VehicleListScreen|VehicleListViewModel|AdminSharedFilters)" --line-buffered 