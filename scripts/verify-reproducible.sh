#!/usr/bin/env bash
# Reproducible build verification for F-Droid.
# Runs two clean release builds from a committed tree and compares
# APK SHA-256 hashes. Must be run from a clean git repo (tag).
set -euo pipefail

if [ -n "$(git status --porcelain)" ]; then
  echo "FAIL: working tree is not clean"
  exit 1
fi

# F-Droid buildserver sets SOURCE_DATE_EPOCH; align it here.
if [ -z "${SOURCE_DATE_EPOCH:-}" ]; then
  export SOURCE_DATE_EPOCH="$(git log -1 --format=%ct)"
  echo "SOURCE_DATE_EPOCH=$SOURCE_DATE_EPOCH"
fi

for i in 1 2; do
  echo "=== Build $i ==="
  ./gradlew clean assembleRelease --no-daemon > /tmp/rb-build-$i.log 2>&1
  find app/build/outputs/apk -name '*.apk' | sort | xargs sha256sum > /tmp/rb-hash-$i.txt
  cat /tmp/rb-hash-$i.txt
done

if diff -u /tmp/rb-hash-1.txt /tmp/rb-hash-2.txt; then
  echo "OK: reproducible (hashes match)"
else
  echo "FAIL: hashes differ"
  exit 1
fi
