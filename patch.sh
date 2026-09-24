#!/bin/bash
set -e

echo "=== Total Stealth Rebrand: VortexPS3 Emu ==="

# 1. Base APK डाउनलोड
echo "[1/5] Downloading Base APK..."
LATEST_URL=$(curl -sL https://api.github.com/repos/Producdevity/gamehub-lite/releases/latest | grep "browser_download_url.*apk" | head -n 1 | cut -d '"' -f 4)
wget -O base.apk "$LATEST_URL"

# 2. Decompile करना
echo "[2/5] Decompiling APK..."
apktool d base.apk -o decompiled_gamehub -f

# 3. लोगो और स्प्लैश को पारदर्शी बनाना
echo "[3/5] Neutralizing Logos & Splash..."
echo "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=" | base64 -d > blank.png

find decompiled_gamehub/res/ -type f \( -iname "*splash*.png" -o -iname "*logo*.png" \) ! -iname "*launcher*" -exec cp blank.png {} \; 2>/dev/null || true

# 4. नाम और क्रेडिट बदलना (Yash / AI VIDEO HUB)
echo "[4/5] Setting Yash as Owner & Developer..."
grep -rli "GameHub Lite" decompiled_gamehub/ | xargs sed -i 's/GameHub Lite/VortexPS3 Emu/gI' 2>/dev/null || true
grep -rli "GameHubLite" decompiled_gamehub/ | xargs sed -i 's/GameHubLite/VortexPS3 Emu/gI' 2>/dev/null || true
grep -rli "GameHub" decompiled_gamehub/ | xargs sed -i 's/GameHub/VortexPS3/gI' 2>/dev/null || true
grep -rli "EmuReady" decompiled_gamehub/ | xargs sed -i 's/EmuReady/Vortex Team/gI' 2>/dev/null || true

CREDIT_REPLACEMENT="VORTEX PS3 EMULATOR - OFFICIAL BUILD\nOWNER & LEAD DEVELOPER: Yash (AI VIDEO HUB)\nProprietary Wine & Turnip Graphics Subsystem"
find decompiled_gamehub/ -type f \( -name "*.smali" -o -name "*.xml" -o -name "*.json" \) -exec sed -i "s/Built on the shoulders of giants.*/$CREDIT_REPLACEMENT/g" {} + 2>/dev/null || true

rm -rf decompiled_gamehub/smali*/com/google/firebase 2>/dev/null || true
rm -rf decompiled_gamehub/smali*/com/umeng 2>/dev/null || true

# 5. Rebuild और Sign
echo "[5/5] Recompiling and Signing APK..."
apktool b decompiled_gamehub -o unsigned.apk

keytool -genkey -v -keystore test.keystore -storepass android -alias androidkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Yash, OU=AI VIDEO HUB, O=VortexPS3 Studios, L=City, S=State, C=IN"
zipalign -p -f 4 unsigned.apk aligned.apk
apksigner sign --ks test.keystore --ks-pass pass:android --out VortexPS3-Ready.apk aligned.apk

echo "=== Success: VortexPS3-Ready.apk Rebranded Successfully ==="
