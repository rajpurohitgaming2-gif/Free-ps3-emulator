#!/bin/bash
set -e

echo "=== Neutralizing Startup Logo to Black/Transparent ==="

# 1. Base APK डाउनलोड
echo "[1/4] Downloading Base APK..."
LATEST_URL=$(curl -sL https://api.github.com/repos/Producdevity/gamehub-lite/releases/latest | grep "browser_download_url.*apk" | head -n 1 | cut -d '"' -f 4)
wget -O base.apk "$LATEST_URL"

# 2. Decompile APK
echo "[2/4] Decompiling APK..."
apktool d base.apk -o decompiled_gamehub -f

# 3. GAMEHUB लोगो को सुरक्षित तरीके से पारदर्शी बनाना (ImageMagick से)
echo "[3/4] Erasing GAMEHUB logo while keeping dimensions..."

# ImageMagick इंस्टॉल/सुनिश्चित करना (Actions रनर में पहले से होता है)
# जो भी इमेज splash, logo या gamehub नाम की है, उसके साइज को छुए बिना उसे पारदर्शी/खाली करना
find decompiled_gamehub/res/ -type f \( -iname "*splash*.png" -o -iname "*logo*.png" -o -iname "*gamehub*.png" \) ! -iname "*launcher*" | while read -r img; do
    convert "$img" -alpha transparent "$img" 2>/dev/null || mogrify -alpha transparent "$img" 2>/dev/null || true
done

find decompiled_gamehub/res/ -type f \( -iname "*splash*.webp" -o -iname "*logo*.webp" -o -iname "*gamehub*.webp" \) ! -iname "*launcher*" | while read -r img; do
    convert "$img" -alpha transparent "$img" 2>/dev/null || mogrify -alpha transparent "$img" 2>/dev/null || true
done

# 4. नाम और अंदरूनी टेक्स्ट बदलना
echo "[4/4] Renaming Strings..."
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/<string name="app_name">.*<\/string>/<string name="app_name">VortexPS3 Emu<\/string>/g' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHub Lite</>Welcome</gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHubLite</>Welcome</gI' {} + 2>/dev/null || true
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i 's/>GameHub</>Welcome</gI' {} + 2>/dev/null || true

# डेवलपर क्रेडिट सेट करना (Yash / AI VIDEO HUB)
CREDIT_REPLACEMENT="VORTEX PS3 EMULATOR - OFFICIAL BUILD\nOWNER & LEAD DEVELOPER: Yash (AI VIDEO HUB)\nProprietary Wine & Turnip Graphics Subsystem"
find decompiled_gamehub/res/values* -name "strings.xml" -exec sed -i "s/>Built on the shoulders of giants.*</>$CREDIT_REPLACEMENT</g" {} + 2>/dev/null || true

# 5. Rebuild और Sign
echo "[5/5] Recompiling and Signing APK..."
apktool b decompiled_gamehub -o unsigned.apk

keytool -genkey -v -keystore test.keystore -storepass android -alias androidkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Yash, OU=AI VIDEO HUB, O=VortexPS3 Studios, L=City, S=State, C=IN"
zipalign -p -f 4 unsigned.apk aligned.apk
apksigner sign --ks test.keystore --ks-pass pass:android --out VortexPS3-Ready.apk aligned.apk

echo "=== Success: VortexPS3-Ready.apk Created Successfully ==="
