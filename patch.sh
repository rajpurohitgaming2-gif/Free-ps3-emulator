#!/bin/bash
set -e

echo "=== Total Stealth Rebrand: VortexPS3 Emu ==="

# 1. Base APK डाउनलोड
echo "[1/5] Downloading Base APK..."
LATEST_URL=$(curl -sL https://api.github.com/repos/Producdevity/gamehub-lite/releases/latest | grep "browser_download_url.*apk" | head -n 1 | cut -d '"' -f 4)
wget -O base.apk "$LATEST_URL"

# 2. Decompile करना (सोर्स कोड छुए बिना सिर्फ जरूरी रिसोर्स खोलना)
echo "[2/5] Decompiling APK..."
apktool d base.apk -o decompiled_gamehub -f

# 3. सिर्फ strings.xml में सुरक्षित तरीके से नाम और क्रेडिट बदलना
echo "[3/5] Setting Yash as Owner & Developer in Strings..."
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/GameHub Lite/VortexPS3 Emu/gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/GameHubLite/VortexPS3 Emu/gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/GameHub/VortexPS3/gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/EmuReady/Vortex Team/gI' {} + 2>/dev/null || true

# डेवलपर क्रेडिट सेट करना
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>Built on the shoulders of giants.*</>VORTEX PS3 EMULATOR - OFFICIAL BUILD | DEVELOPER: Yash (AI VIDEO HUB)</g' {} + 2>/dev/null || true

# 4. Rebuild और Sign
echo "[4/5] Recompiling and Signing APK..."
apktool b decompiled_gamehub -o unsigned.apk

keytool -genkey -v -keystore test.keystore -storepass android -alias androidkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Yash, OU=AI VIDEO HUB, O=VortexPS3 Studios, L=City, S=State, C=IN"
zipalign -p -f 4 unsigned.apk aligned.apk
apksigner sign --ks test.keystore --ks-pass pass:android --out VortexPS3-Ready.apk aligned.apk

echo "=== Success: VortexPS3-Ready.apk Rebranded Successfully ==="
