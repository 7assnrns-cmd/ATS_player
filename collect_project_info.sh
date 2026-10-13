#!/bin/bash
set -e
cd ~/ArchiveTune_S

# تحديد مجلد التنزيلات
if [ -d "$HOME/storage/downloads" ]; then
    DOWNLOADS="$HOME/storage/downloads"
elif [ -d "/sdcard/Download" ]; then
    DOWNLOADS="/sdcard/Download"
else
    echo "⚠️ لم يتم العثور على مجلد التنزيلات. سيتم الحفظ في المجلد الحالي."
    DOWNLOADS="."
fi

OUTPUT="$DOWNLOADS/project_info_$(date +%Y%m%d_%H%M%S).txt"
echo "📁 سيتم حفظ الملف في: $OUTPUT"
echo

exec > >(tee "$OUTPUT") 2>&1

echo "=== Project Info ==="
echo "Date: $(date)"
echo "PWD: $(pwd)"
echo

echo "=== Git Status ==="
git status --short
echo

echo "=== Git Diff Stat ==="
git diff --stat
echo

echo "=== Directory Tree (depth 4, excluding build) ==="
find . -maxdepth 4 -type d \( -name build -o -name .gradle -o -name .git -o -name .idea \) -prune -o -type f -print | sed 's|^\./||' | sort | head -200
echo

echo "=== Gradle Files ==="
for f in build.gradle.kts settings.gradle.kts gradle.properties gradle/libs.versions.toml; do
    if [ -f "$f" ]; then
        echo "--- $f ---"
        cat "$f"
        echo
    fi
done
echo

echo "=== Kotlin Files in ui/ ==="
find app/src/main/kotlin/moe/rukamori/archivetune/ui -type f -name '*.kt' | sort | while read f; do
    echo "--- $f ---"
    cat "$f"
    echo
done

echo "=== MainActivity.kt ==="
cat app/src/main/kotlin/moe/rukamori/archivetune/MainActivity.kt
echo

echo "=== Grep for keywords ==="
for kw in appBackdrop liquid safeDrawing betaUpdates Ghosting TopAppBar; do
    echo "--- $kw ---"
    grep -rn "$kw" app/src/main/kotlin/ || true
    echo
done

echo "=== Brace balance for all Kotlin files ==="
find app/src/main/kotlin -name '*.kt' | while read f; do
    python3 -c "
s=open('$f').read(); d=0
for ch in s:
    if ch=='{': d+=1
    elif ch=='}': d-=1
print(f'{d:>3}  $f')
"
done
echo

echo "=== Build Output (last 150 lines) ==="
./gradlew :app:compileGmsMobileUniversalDebugKotlin --console=plain > build_output.log 2>&1 || true
tail -150 build_output.log
echo

echo "=== Done ==="
