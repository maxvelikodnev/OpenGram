#!/usr/bin/env python3
"""Verify the installed release's high account slots using a temporary signed test APK."""
import os
from pathlib import Path
import subprocess
import sys
import zipfile

root = Path(__file__).resolve().parent.parent
props = dict(line.split("=", 1) for line in (root / "local.properties").read_text().splitlines()
             if "=" in line and not line.startswith("#"))
sdk = Path(props["sdk.dir"])
tools = sdk / "build-tools/36.0.0"
android = sdk / "platforms/android-36/android.jar"
java = Path(os.environ.get("OPENGRAM_JAVA_HOME", "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"))
env = dict(os.environ, JAVA_HOME=str(java), PATH=str(java / "bin") + ":" + os.environ["PATH"],
           OPENGRAM_TEST_STORE_PASSWORD=props["OPENGRAM_STORE_PASSWORD"],
           OPENGRAM_TEST_KEY_PASSWORD=props["OPENGRAM_KEY_PASSWORD"])
serial = sys.argv[1]
work = root / "build/account-device-test"
classes = work / "classes"
dex = work / "dex"
classes.mkdir(parents=True, exist_ok=True)
dex.mkdir(parents=True, exist_ok=True)
unsigned = work / "unsigned.apk"
signed = work / "account-tests.apk"

def run(*command):
    return subprocess.run([str(arg) for arg in command], env=env, check=True,
                          capture_output=True, text=True).stdout

run(java / "bin/javac", "-source", "8", "-target", "8", "-cp", android, "-d", classes,
    root / "tests/accounts/android/AccountInstrumentation.java")
run(tools / "d8", "--min-api", "23", "--lib", android, "--output", dex, *classes.rglob("*.class"))
run(tools / "aapt2", "link", "-I", android, "--manifest",
    root / "tests/accounts/android/AndroidManifest.xml", "-o", unsigned)
with zipfile.ZipFile(unsigned, "a", compression=zipfile.ZIP_DEFLATED) as apk:
    for file in dex.glob("*.dex"):
        apk.write(file, file.name)
run(tools / "apksigner", "sign", "--ks", (root / props["OPENGRAM_KEYSTORE"]).resolve(),
    "--ks-key-alias", props["OPENGRAM_KEY_ALIAS"],
    "--ks-pass", "env:OPENGRAM_TEST_STORE_PASSWORD",
    "--key-pass", "env:OPENGRAM_TEST_KEY_PASSWORD", "--out", signed, unsigned)
adb = [sdk / "platform-tools/adb", "-s", serial]
run(*adb, "install", "-r", "-t", signed)
try:
    result = run(*adb, "shell", "am", "instrument", "-w", "-r",
                 "app.opengram.android.accounttests/app.opengram.accounttests.AccountInstrumentation")
    (work / "result.txt").write_text(result)
    print(result)
    if "PASS: installed release" not in result:
        raise SystemExit("Device account verification failed; see build/account-device-test/result.txt")
finally:
    # Only remove the temporary test package created by this script.
    run(*adb, "uninstall", "app.opengram.android.accounttests")
