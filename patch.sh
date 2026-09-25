#!/bin/bash
set -e

echo "=== Total Rebrand: Setting WELCOME & VortexPS3 ==="

# 1. Base APK Download
echo "[1/4] Downloading Base APK..."
LATEST_URL=$(curl -sL https://api.github.com/repos/Producdevity/gamehub-lite/releases/latest | grep "browser_download_url.*apk" | head -n 1 | cut -d '"' -f 4)
wget -O base.apk "$LATEST_URL"

# 2. Decompile APK
echo "[2/4] Decompiling APK..."
apktool d base.apk -o decompiled_gamehub -f

# 3. Safe string replacement (No layout touch)
echo "[3/4] Renaming to WELCOME & VortexPS3 Emu..."

# App Name change
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/<string name="app_name">.*<\/string>/<string name="app_name">VortexPS3 Emu<\/string>/g' {} + 2>/dev/null || true

# Display strings me GameHub ki jagah WELCOME
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHub Lite</>WELCOME</gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHubLite</>WELCOME</gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHub</>WELCOME</gI' {} + 2>/dev/null || true

# Smali code strings safe update
find decompiled_gamehub/smali* -type f -name "*.smali" -exec sed -i 's/"GameHub Lite"/"WELCOME"/gI' {} + 2>/dev/null || true
find decompiled_gamehub/smali* -type f -name "*.smali" -exec sed -i 's/"GameHubLite"/"WELCOME"/gI' {} + 2>/dev/null || true
find decompiled_gamehub/smali* -type f -name "*.smali" -exec sed -i 's/"GameHub"/"WELCOME"/gI' {} + 2>/dev/null || true

# 4. Rebuild aur Sign
echo "[4/4] Recompiling and Signing APK..."
apktool b decompiled_gamehub -o unsigned.apk

keytool -genkey -v -keystore test.keystore -storepass android -alias androidkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Yash, OU=AI VIDEO HUB, O=VortexPS3 Studios, L=City, S=State, C=IN"
zipalign -p -f 4 unsigned.apk aligned.apk
apksigner sign --ks test.keystore --ks-pass pass:android --out VortexPS3-Ready.apk aligned.apk

echo "=== Success: VortexPS3-Ready.apk Created Successfully ==="
