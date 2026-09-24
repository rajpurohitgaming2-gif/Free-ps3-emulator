#!/bin/bash
set -e

echo "=== GameHub Lite Build System ==="

# 1. Base GameHub Lite APK डाउनलोड करना
echo "[1/5] Downloading Latest GameHub Lite APK..."
LATEST_URL=$(curl -s https://api.github.com/repos/Producdevity/gamehub-lite/releases/latest | grep "browser_download_url.*apk" | head -n 1 | cut -d '"' -f 4)

if [ -z "$LATEST_URL" ]; then
    LATEST_URL="https://github.com/Producdevity/gamehub-lite/releases/download/v5.1.8/GameHub.Lite.v5.1.8.apk"
fi

wget -O base.apk "$LATEST_URL"

# 2. Decompile करना
echo "[2/5] Decompiling with Apktool..."
apktool d base.apk -o decompiled_gamehub -f

# 3. अपना नाम और क्रेडिट जोड़ना
echo "[3/5] Adding Custom Credit & Name..."
find decompiled_gamehub/res -name "strings.xml" -exec sed -i 's/Built on the shoulders of giants/Custom Mod by Yash (AI VIDEO HUB) - Built on the shoulders of giants/g' {} + || true

# ब्लोटवेयर हटाना
rm -rf decompiled_gamehub/smali*/com/google/firebase 2>/dev/null || true
rm -rf decompiled_gamehub/smali*/com/umeng 2>/dev/null || true

# 4. APK Rebuild
echo "[4/5] Recompiling APK..."
apktool b decompiled_gamehub -o unsigned.apk

# 5. Keystore & Sign
echo "[5/5] Aligning and Signing APK..."
keytool -genkey -v -keystore test.keystore -storepass android -alias androidkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Yash, OU=AIVIDEOHUB, O=VortexPC, L=City, S=State, C=IN"
zipalign -p -f 4 unsigned.apk aligned.apk
apksigner sign --ks test.keystore --ks-pass pass:android --out GameHub-Lite-Ready.apk aligned.apk

echo "=== Done! GameHub-Lite-Ready.apk Created Successfully ==="
