#!/usr/bin/env bash
# Reproducible build verification for F-Droid.
# Runs two clean release builds from a committed tree and compares
# APK SHA-256 hashes. Must be run from a clean git repo (tag).
set -euo pipefail

if [ -n "$(git status --porcelain)" ]; then
  echo "FAIL: working tree is not clean"
  exit 1
fi

# Signed APKs are NOT reproducible: AGP 8.x signs with RSA-PSS, whose random
# salt lands in the APK Signing Block, so two clean builds of the same commit
# hash differently even though every zip entry is byte-identical (measured on
# this repo: 5f61f0fb... vs 537ec282...). F-Droid's own check strips signatures
# (apksigcopier), so we do the same by hiding the keystore for the duration.
KS=keystore.properties
KS_TMP=.keystore.properties.repro-hidden
restore_ks() {
  # Note: plain `[ -f ... ] && mv ...` would leave the function (and thus a
  # `set -e` script exiting through this trap) with status 1 whenever there
  # is nothing to restore — turning a successful run into exit code 1.
  if [ -f "$KS_TMP" ]; then mv "$KS_TMP" "$KS"; fi
}
trap restore_ks EXIT
if [ -f "$KS" ]; then mv "$KS" "$KS_TMP"; echo "hid $KS (comparing unsigned APKs)"; fi

# F-Droid buildserver sets SOURCE_DATE_EPOCH; align it here.
if [ -z "${SOURCE_DATE_EPOCH:-}" ]; then
  export SOURCE_DATE_EPOCH="$(git log -1 --format=%ct)"
  echo "SOURCE_DATE_EPOCH=$SOURCE_DATE_EPOCH"
fi

for i in 1 2; do
  echo "=== Build $i ==="
  ./gradlew clean assembleRelease --no-daemon > /tmp/rb-build-$i.log 2>&1
  ls app/build/outputs/apk/release/*unsigned*.apk >/dev/null 2>&1 \
    || { echo "FAIL: no unsigned APK produced"; exit 1; }
find app/build/outputs/apk -name '*-unsigned.apk' | sort | xargs sha256sum > /tmp/rb-hash-$i.txt
  cat /tmp/rb-hash-$i.txt
done

if diff -u /tmp/rb-hash-1.txt /tmp/rb-hash-2.txt; then
  echo "OK: reproducible (hashes match)"
else
  echo "FAIL: hashes differ"
  exit 1
fi
