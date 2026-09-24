#!/bin/bash
set -e

echo "=== VortexPS3 Emu Build System ==="

# 1. Base GameHub Lite APK डाउनलोड करना
echo "[1/5] Downloading Base APK..."
LATEST_URL=$(curl -s https://api.github.com/repos/Producdevity/gamehub-lite/releases/latest | grep "browser_download_url.*apk" | head -n 1 | cut -d '"' -f 4)

if [ -z "$LATEST_URL" ]; then
    LATEST_URL="https://github.com/Producdevity/gamehub-lite/releases/download/v5.1.8/GameHub.Lite.v5.1.8.apk"
fi

wget -O base.apk "$LATEST_URL"

# 2. Decompile करना
echo "[2/5] Decompiling with Apktool..."
apktool d base.apk -o decompiled_gamehub -f

# 3. ऐप का नाम और क्रेडिट बदलना
echo "[3/5] Setting App Name to VortexPS3 Emu..."
NEW_APP_NAME="VortexPS3 Emu"

# फोन स्क्रीन पर दिखने वाला नाम बदलना
find decompiled_gamehub/res -name "strings.xml" -exec sed -i "s|<string name=\"app_name\">.*</string>|<string name=\"app_name\">$NEW_APP_NAME</string>|g" {} + || true

# क्रेडिट स्क्रीन में आपका नाम जोड़ना
find decompiled_gamehub/res -name "strings.xml" -exec sed -i 's/Built on the shoulders of giants/VortexPS3 Emu - Modded by Yash (AI VIDEO HUB) - Built on the shoulders of giants/g' {} + || true

# ब्लोटवेयर हटाना
rm -rf decompiled_gamehub/smali*/com/google/firebase 2>/dev/null || true
rm -rf decompiled_gamehub/smali*/com/umeng 2>/dev/null || true

# 4. APK Rebuild
echo "[4/5] Recompiling APK..."
apktool b decompiled_gamehub -o unsigned.apk

# 5. Keystore & Sign
echo "[5/5] Aligning and Signing APK..."
keytool -genkey -v -keystore test.keystore -storepass android -alias androidkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Yash, OU=AIVIDEOHUB, O=VortexPS3, L=City, S=State, C=IN"
zipalign -p -f 4 unsigned.apk aligned.apk
apksigner sign --ks test.keystore --ks-pass pass:android --out VortexPS3-Ready.apk aligned.apk

echo "=== Done! VortexPS3-Ready.apk Created Successfully ==="
