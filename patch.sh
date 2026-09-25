#!/bin/bash
set -e

echo "=== Rebrand: Splash Screen to Welcome & VortexPS3 ==="

# 1. Base APK डाउनलोड
echo "[1/4] Downloading Base APK..."
LATEST_URL=$(curl -sL https://api.github.com/repos/Producdevity/gamehub-lite/releases/latest | grep "browser_download_url.*apk" | head -n 1 | cut -d '"' -f 4)
wget -O base.apk "$LATEST_URL"

# 2. Decompile APK
echo "[2/4] Decompiling APK..."
apktool d base.apk -o decompiled_gamehub -f

# 3. स्प्लैश और स्टार्टअप स्क्रीन को बदलना (GAMEHUB हटाकर Welcome करना)
echo "[3/4] Replacing Startup Brand with Welcome..."

# स्प्लैश/स्टार्टअप से जुड़ी लेआउट XML में GAMEHUB टेक्स्ट को Welcome बनाना
find decompiled_gamehub/res/layout* -name "*splash*.xml" -o -name "*launch*.xml" -o -name "*startup*.xml" | while read -r file; do
    sed -i 's/android:text=".*GAMEHUB.*"/android:text="Welcome"/gI' "$file" 2>/dev/null || true
done

# सामान्य स्ट्रिंग्स में नाम बदलना
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/<string name="app_name">.*<\/string>/<string name="app_name">VortexPS3 Emu<\/string>/g' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHub Lite</>Welcome</gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHubLite</>Welcome</gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHub</>Welcome</gI' {} + 2>/dev/null || true

# कोड के अंदर मौजूद GameHub को Welcome बनाना
find decompiled_gamehub/smali* -type f -name "*.smali" -exec sed -i 's/"GameHub Lite"/"Welcome"/gI' {} + 2>/dev/null || true
find decompiled_gamehub/smali* -type f -name "*.smali" -exec sed -i 's/"GameHubLite"/"Welcome"/gI' {} + 2>/dev/null || true
find decompiled_gamehub/smali* -type f -name "*.smali" -exec sed -i 's/"GameHub"/"Welcome"/gI' {} + 2>/dev/null || true

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
