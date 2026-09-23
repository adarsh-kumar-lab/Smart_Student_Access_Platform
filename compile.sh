#!/bin/bash
# Compiles all Java source files into the "out" folder.
# -encoding UTF-8 is REQUIRED so the emoji icons in titles/menus render correctly.

mkdir -p out
javac -encoding UTF-8 -d out src/*.java

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful! Run ./run.sh to start the app."
else
    echo ""
    echo "❌ Build failed. Scroll up to see the compiler error."
fi
