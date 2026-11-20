#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
task_java="${OPENGRAM_JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
task_dir="$(mktemp -d "${TMPDIR:-/tmp}/opengram-account-tests.XXXXXX")"
trap 'rm -rf "$task_dir"' EXIT
"$task_java/bin/javac" -d "$task_dir" \
    TMessagesProj/src/main/java/org/telegram/messenger/AccountStore.java \
    TMessagesProj/src/main/java/org/telegram/messenger/AccountSlots.java \
    tests/accounts/AccountStorageTest.java
"$task_java/bin/java" -cp "$task_dir" AccountStorageTest
"${CXX:-/usr/bin/clang++}" -std=c++17 -pthread \
    -I TMessagesProj/jni/tgnet tests/accounts/native_registry_test.cpp \
    -o "$task_dir/native_registry_test"
"$task_dir/native_registry_test"
