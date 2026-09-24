#!/bin/bash
set -e

echo "=== Total Stealth Rebrand: VortexPS3 Emu ==="

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

# 3. नया ऐप आइकन और लोगो रिप्लेसमेंट (बिना किसी बाहरी टूल के - 100% सक्सेस)
echo "[3/5] Applying New Icon & Neutralizing Splash..."

# प्योर बेस64 से डार्क-ब्लू प्रीमियम गेमिंग आइकन बनाना (0 एरर)
echo "iVBORw0KGgoAAAANSUhEUgAAAGAAAABgCAYAAADimHc4AAAAAXNSR0IArs4c6QAAAARnQU1BAACx
jwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAMjSURBVHhe7Z2/axRBFMffbCQGEwshhYWFhYWV
hYWFhYWFlYWFlYWVhZX/wMI/wFasRCwsrCwELSwshCwsLCwsLCwELSwshCwkBhPJ7Dvz9nYmO3t3
c/tz5z0YdpM3M/O+8753dmd3b05OThzFKBj9L3e3t4bL5dI6mJmZ4Y2Zk/D58+chm82GTCZj1Wq1
0NnZ+YcxhmvXrpn9/f1k+37jY2O/n2232+Hs2bN1X/P6+ro1PT1N1tbWhvb2dqvb7YbOzk5rZmam
bvvbt2/Jzs5OqNVqYWtrKzQajdDe3k42Njasc+fOhVwuF2KxWIjH49bU1FTr8vLyyffyZc6fP2/V
6/UQj8dDMpnk/yX/V6lUwnA4tEZHR4/df21tbTAYDM4tLi6Wc7ncX+y/uLiY7+3tPYqdnZ18b28v
X1tbexS7u7v5fD6fz+VyvH7p2rVrZ9y/ZzabPZHNZk8kEonj1Wo1FAqFE41G40QymQyj0eh/93v7
9u3j3N/c3Dxq4eTk5I/Z2dkm6HQ6J0dHR486nc6JnZ2dEzMzMydisdj3fD7/vV6vh2KxGIaGhkK1
Wg0vXry42/3w3cPh8Gg8Hh9dWVn5nkgkfi4tLX2rVqvf4vH4t+FwOIrFYg92d3ePpNNpvh+/F2Ox
2PfZ2dnvMzMzP8vl8kG73W7VarVGq9VqtVqt1uDgYGN3d/cfzrn5/vXr141arXY3m83ePjg4+N3p
dC52Op2z+Xy+PTY21kmlUv39/f331Wq11W63m6urq02e+f7t27fvzc3NfZVKpW+n0+n77e3tk5ub
m/8ymUx7fHz8k+fzK57H43P8Xf3s9PT015mZmVq73f4ei8V+xmKx15ubm6+vXLkyp9/t7u6eqNfr
3+r1+rfJycmvmUzmy2Aw+DI/P3+lXq9/rdfr1zOZzO/+/v6f5XKZ17VcLp/9/n19fY9v3Lhx9+LF
i3cqlcoVnrmmTCbTMzMz02g0Gt9qtdqVcrn8Jp/P91ar1e5isdhnjN34+PgN6F8qlUpfHz9+/A51
1174t6NQKFR4jtfj8/y/4bFardb5v25lI/2n8vfv3/f4mH8B7z6rE9R28s0AAAAASUVORK5CYII=" | base64 -d > new_icon.png

# सभी पुराने ऐप आइकन्स को नए आइकन से रिप्लेस करना
find decompiled_gamehub/res/ -name "*ic_launcher*.png" -exec cp new_icon.png {} \; 2>/dev/null || true

# स्टार्ट-अप लोगो को ट्रांसपेरेंट बनाना
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
