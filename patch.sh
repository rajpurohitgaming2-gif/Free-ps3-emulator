#!/bin/bash
set -e

echo "=== Total Stealth Rebrand + Custom App Icon ==="

# 1. Base Runtime डाउनलोड
echo "[1/5] Downloading Base APK..."
LATEST_URL=$(curl -s https://api.github.com/repos/Producdevity/gamehub-lite/releases/latest | grep "browser_download_url.*apk" | head -n 1 | cut -d '"' -f 4)

if [ -z "$LATEST_URL" ]; then
    LATEST_URL="https://github.com/Producdevity/gamehub-lite/releases/download/v5.1.8/GameHub.Lite.v5.1.8.apk"
fi

wget -O base.apk "$LATEST_URL"

# 2. Decompile करना
echo "[2/5] Decompiling APK..."
apktool d base.apk -o decompiled_gamehub -f

# 3. नया ऐप आइकन और लोगो रिप्लेसमेंट
echo "[3/5] Replacing App Launcher Icons & Neutralizing Splash..."

# एक नया प्रीमियम गेमिंग आइकन डाउनलोड करना
wget -O new_icon.png "https://raw.githubusercontent.com/google/material-design-icons/master/png/hardware/videogame_asset/materialicons/48dp/2x/baseline_videogame_asset_white_48dp.png"

# सभी पुराने लॉन्चर आइकन्स (App Icon) को नए आइकन से रिप्लेस करना
find decompiled_gamehub/res/ -name "*ic_launcher*.png" -exec cp new_icon.png {} \; 2>/dev/null || true

# स्टार्ट-अप स्प्लैश लोगो को ट्रांसपेरेंट बनाना
find decompiled_gamehub/res/ -iname "*splash*" -o -iname "*logo*" | while read -r img; do
    if [[ "$img" == *.png ]]; then
        echo -ne '\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x06\x00\x00\x00\x1f\x15c4\x00\x00\x00\nIDATx\x9cc\x00\x01\x00\x00\x05\x00\x01\r\n-\xb4\x00\x00\x00\x00IEND\xaeB`\x82' > "$img" || true
    fi
done

# Steam विजेट को छुपाना
find decompiled_gamehub/res/layout -name "*.xml" -exec sed -i 's/android:id="@id\/steam/android:visibility="gone" android:id="@id\/steam/g' {} + 2>/dev/null || true

# 4. नाम और क्रेडिट बदलना (Yash / AI VIDEO HUB)
echo "[4/5] Setting Yash as Owner & Developer..."

grep -rli "GameHub Lite" decompiled_gamehub/ | xargs sed -i 's/GameHub Lite/VortexPS3 Emu/gI' 2>/dev/null || true
grep -rli "GameHubLite" decompiled_gamehub/ | xargs sed -i 's/GameHubLite/VortexPS3 Emu/gI' 2>/dev/null || true
grep -rli "GameHub" decompiled_gamehub/ | xargs sed -i 's/GameHub/VortexPS3/gI' 2>/dev/null || true
grep -rli "EmuReady" decompiled_gamehub/ | xargs sed -i 's/EmuReady/Vortex Team/gI' 2>/dev/null || true

CREDIT_REPLACEMENT="VORTEX PS3 EMULATOR - OFFICIAL BUILD\nOWNER & LEAD DEVELOPER: Yash (AI VIDEO HUB)\nProprietary Wine & Turnip Graphics Subsystem"
find decompiled_gamehub/ -type f \( -name "*.smali" -o -name "*.xml" -o -name "*.json" \) -exec sed -i "s/Built on the shoulders of giants.*/$CREDIT_REPLACEMENT/g" {} + 2>/dev/null || true

# ट्रैकर हटाना
rm -rf decompiled_gamehub/smali*/com/google/firebase 2>/dev/null || true
rm -rf decompiled_gamehub/smali*/com/umeng 2>/dev/null || true

# 5. Rebuild और Sign
echo "[5/5] Recompiling and Signing APK..."
apktool b decompiled_gamehub -o unsigned.apk

keytool -genkey -v -keystore test.keystore -storepass android -alias androidkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Yash, OU=AI VIDEO HUB, O=VortexPS3 Studios, L=City, S=State, C=IN"
zipalign -p -f 4 unsigned.apk aligned.apk
apksigner sign --ks test.keystore --ks-pass pass:android --out VortexPS3-Ready.apk aligned.apk

echo "=== Success: VortexPS3-Ready.apk with New Icon Created! ==="
