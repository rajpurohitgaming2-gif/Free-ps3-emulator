#!/bin/bash
set -e

echo "=== Total Stealth Rebrand: VortexPS3 Emu ==="

# 1. Base APK डाउनलोड
echo "[1/4] Downloading Base APK..."
LATEST_URL=$(curl -sL https://api.github.com/repos/Producdevity/gamehub-lite/releases/latest | grep "browser_download_url.*apk" | head -n 1 | cut -d '"' -f 4)
wget -O base.apk "$LATEST_URL"

# 2. Decompile
echo "[2/4] Decompiling APK..."
apktool d base.apk -o decompiled_gamehub -f

# 3. नाम और हार्डकोडेड टेक्स्ट बदलना
echo "[3/4] Replacing In-App Text to VortexPS3 Emu..."

# App Name और Strings बदलना
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/<string name="app_name">.*<\/string>/<string name="app_name">VortexPS3 Emu<\/string>/g' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHub Lite</>VortexPS3 Emu</gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHubLite</>VortexPS3 Emu</gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHub</>VortexPS3</gI' {} + 2>/dev/null || true

# लेआउट XML में हार्डकोडेड टेक्स्ट बदलना (android:text="GameHubLite" को बदलना)
find decompiled_gamehub/res/layout* -name "*.xml" -exec sed -i 's/android:text="GameHub Lite"/android:text="VortexPS3 Emu"/gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/layout* -name "*.xml" -exec sed -i 's/android:text="GameHubLite"/android:text="VortexPS3 Emu"/gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/layout* -name "*.xml" -exec sed -i 's/android:text="GameHub"/android:text="VortexPS3"/gI' {} + 2>/dev/null || true

# Smali कोड के अंदर मौजूद हार्डकोडेड स्ट्रिंग्स बदलना (सुरक्षित तरीके से)
find decompiled_gamehub/smali* -type f -name "*.smali" -exec sed -i 's/const-string v[0-9]*, "GameHub Lite"/const-string v0, "VortexPS3 Emu"/gI' {} + 2>/dev/null || true
find decompiled_gamehub/smali* -type f -name "*.smali" -exec sed -i 's/const-string v[0-9]*, "GameHubLite"/const-string v0, "VortexPS3 Emu"/gI' {} + 2>/dev/null || true
find decompiled_gamehub/smali* -type f -name "*.smali" -exec sed -i 's/"GameHub Lite"/"VortexPS3 Emu"/gI' {} + 2>/dev/null || true
find decompiled_gamehub/smali* -type f -name "*.smali" -exec sed -i 's/"GameHubLite"/"VortexPS3 Emu"/gI' {} + 2>/dev/null || true

# डेवलपर क्रेडिट सेट करना (Yash / AI VIDEO HUB)
CREDIT_REPLACEMENT="VORTEX PS3 EMULATOR - OFFICIAL BUILD\nOWNER & LEAD DEVELOPER: Yash (AI VIDEO HUB)\nProprietary Wine & Turnip Graphics Subsystem"
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i "s/>Built on the shoulders of giants.*</>$CREDIT_REPLACEMENT</g" {} + 2>/dev/null || true

# 4. Rebuild और Sign
echo "[4/4] Recompiling and Signing APK..."
apktool b decompiled_gamehub -o unsigned.apk

keytool -genkey -v -keystore test.keystore -storepass android -alias androidkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Yash, OU=AI VIDEO HUB, O=VortexPS3 Studios, L=City, S=State, C=IN"
zipalign -p -f 4 unsigned.apk aligned.apk
apksigner sign --ks test.keystore --ks-pass pass:android --out VortexPS3-Ready.apk aligned.apk

echo "=== Success: VortexPS3-Ready.apk Created Successfully ==="
