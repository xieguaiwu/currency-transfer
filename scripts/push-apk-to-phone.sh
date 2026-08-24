#!/usr/bin/env bash
# Push the latest debug APK to a connected Android device's Download folder.
# Usage: ./scripts/push-apk-to-phone.sh
# Requires: adb (Android platform-tools), a device with USB debugging enabled.
set -euo pipefail

SDK="${ANDROID_HOME:-$HOME/Android/Sdk}"
ADB="$SDK/platform-tools/adb"
APK="$SDK/../Desktop/android-projects/currency-transfer/app/build/outputs/apk/debug/app-debug.apk"
# fallback: compute relative to script
if [ ! -f "$APK" ]; then
  ROOT="$(cd "$(dirname "$0")/.." && pwd)"
  APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
fi

if [ ! -f "$APK" ]; then
  echo "FAIL: APK not found at $APK. Build it first: ./gradlew assembleDebug"
  exit 1
fi

echo "== Waiting for device =="
"$ADB" wait-for-device

echo "== Device list =="
"$ADB" devices -l

echo "== Pushing $APK to /sdcard/Download/ =="
"$ADB" push "$APK" /sdcard/Download/FX-Pixel-debug.apk

echo "== Verifying =="
"$ADB" shell ls -la /sdcard/Download/FX-Pixel-debug.apk

echo "OK: APK copied to phone Download folder."
echo "Install:  adb shell pm install -r /sdcard/Download/FX-Pixel-debug.apk"
