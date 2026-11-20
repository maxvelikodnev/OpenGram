#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

export JAVA_HOME="${OPENGRAM_JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
export GRADLE_USER_HOME="$PWD/.gradle-home"
export PATH="$JAVA_HOME/bin:$PATH"
if [[ ! -x "$JAVA_HOME/bin/java" ]]; then
    echo 'Java 17 required. Set OPENGRAM_JAVA_HOME to its installation directory.' >&2
    exit 1
fi
case "${1:-prepare}" in
    prepare)
        exec ./gradlew --console=plain --max-workers=4 prepareOpenGram
        ;;
    apk)
        exec ./gradlew --console=plain --max-workers=4 :TMessagesProj_App:assembleAfatRelease
        ;;
    *)
        exec ./gradlew --console=plain --max-workers=4 "$@"
        ;;
esac
