#!/usr/bin/env bash
set -euo pipefail

: "${ANDROID_HOME:?Set ANDROID_HOME to your Android SDK path}"

if ! command -v gradle >/dev/null 2>&1; then
  echo "gradle not found. Install Gradle 8+ or use gradle wrapper." >&2
  exit 1
fi

gradle assembleDebug
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [[ ! -f "$APK_PATH" ]]; then
  echo "APK not generated at $APK_PATH" >&2
  exit 1
fi

echo "APK built: $APK_PATH"

if command -v adb >/dev/null 2>&1; then
  adb install -r "$APK_PATH"
  echo "Installed on connected device."
else
  echo "adb not found; skipping install."
fi
