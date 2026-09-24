#!/bin/bash
set -e

echo "=== GameHub Lite Build System ==="

# 1. Base GameHub 5.1.0 APK डाउनलोड करना
echo "[1/5] Downloading Base APK..."
wget -q -O base.apk "https://github.com/Producdevity/gamehub-lite/releases/download/v5.1.8/GameHub.Lite.v5.1.8.apk" || {
    echo "Fallback: Downloading alternative base..."
    wget -q -O base.apk "https://github.com/gamehub-oss/gamehub-oss/releases/download/v1.0/gamehub.apk"
}

# 2. Decompile करना
echo "[2/5] Decompiling with Apktool..."
apktool d base.apk -o decompiled_gamehub -f

# 3. Telemetry, ब्लोटवेयर और गैर-जरूरी पैकेज हटाना
echo "[3/5] Applying Lite Mod & Cleaning Bloat..."
rm -rf decompiled_gamehub/smali*/com/google/firebase 2>/dev/null || true
rm -rf decompiled_gamehub/smali*/com/umeng 2>/dev/null || true
rm -rf decompiled_gamehub/smali*/com/tencent 2>/dev/null || true

# 4. APK को दोबारा जोड़ना (Rebuild)
echo "[4/5] Recompiling GameHub Lite APK..."
apktool b decompiled_gamehub -o unsigned.apk

# 5. Keystore बनाकर APK को साइन करना
echo "[5/5] Aligning and Signing APK..."
keytool -genkey -v -keystore test.keystore -storepass android -alias androidkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=GameHubLite, OU=Dev, O=Mod, L=City, S=State, C=IN"
zipalign -p -f 4 unsigned.apk aligned.apk
apksigner sign --ks test.keystore --ks-pass pass:android --out GameHub-Lite-Ready.apk aligned.apk

echo "=== Done! GameHub-Lite-Ready.apk Created Successfully ==="

