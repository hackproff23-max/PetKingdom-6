#!/usr/bin/env bash
# Build local (precisa de JDK 21 e Android SDK; ANDROID_HOME configurado).
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJ="$HERE/../build/J2ME-Loader"
bash "$HERE/apply.sh" "$PROJ"
bash "$HERE/sign-setup.sh" "$PROJ"
cd "$PROJ"
chmod +x gradlew
./gradlew assembleMidletRelease
mkdir -p "$HERE/../dist"
cp app/build/outputs/apk/midlet/release/*.apk "$HERE/../dist/"
ls -la "$HERE/../dist"
